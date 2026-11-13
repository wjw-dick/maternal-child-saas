package com.quantaida.mc.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 领域事件总线。
 *
 * <p>启用 RabbitMQ 时投递到 topic exchange，路由键 {@code mc.<type>}，消息体为 JSON，
 * 并携带 tenant_id 头；未启用或连接失败时只记录本地日志，不阻断主业务。
 * 生产环境通过持久化消息 + 手动 ACK + 死信队列保证可靠消费。</p>
 */
@Slf4j
@Component
public class EventBus {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectProvider<RabbitTemplate> rabbitProvider;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final boolean enabled;
    private final AtomicLong downUntil = new AtomicLong(0);

    public EventBus(ObjectProvider<RabbitTemplate> rabbitProvider,
                    ObjectMapper objectMapper,
                    @Value("${app.mq.exchange:mc.topic}") String exchange,
                    @Value("${app.mq.enabled:false}") boolean enabled) {
        this.rabbitProvider = rabbitProvider;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.enabled = enabled;
    }

    public void publish(String type, Map<String, Object> payload) {
        publish(type, payload, 0);
    }

    /**
     * 通过 TTL + 死信队列实现延迟消息（无需 RabbitMQ 延迟插件）。
     *
     * @param delayMillis 延迟毫秒数，必须大于 0
     */
    public void publishDelayed(String type, Map<String, Object> payload, long delayMillis) {
        publish(type, payload, Math.max(delayMillis, 1000L));
    }

    private void publish(String type, Map<String, Object> payload, long delayMillis) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID().toString().replace("-", ""));
        event.put("type", type);
        event.put("tenantId", TenantContext.getTenantId());
        event.put("occurredAt", LocalDateTime.now().format(TS));
        event.put("payload", payload);
        if (!enabled || System.currentTimeMillis() < downUntil.get()) {
            log.info("[EVENT-LOCAL] {} tenant={} payload={}", type, TenantContext.getTenantId(), payload);
            return;
        }
        try {
            String body = objectMapper.writeValueAsString(event);
            MessagePostProcessor processor = message -> {
                MessageProperties props = message.getMessageProperties();
                props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                props.setContentEncoding("UTF-8");
                props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                props.setHeader("tenantId", TenantContext.getTenantId());
                props.setHeader("eventType", type);
                if (delayMillis > 0) {
                    props.setExpiration(String.valueOf(delayMillis));
                }
                return message;
            };
            if (delayMillis > 0) {
                rabbitProvider.getObject().convertAndSend("", "mc.delay.queue", body, processor);
            } else {
                rabbitProvider.getObject().convertAndSend(exchange, "mc." + type, body, processor);
            }
        } catch (Exception e) {
            downUntil.set(System.currentTimeMillis() + 30_000L);
            log.warn("RabbitMQ 投递失败，已降级本地日志 30 秒: {}", e.getMessage());
            log.info("[EVENT-FALLBACK] {} tenant={} payload={}", type, TenantContext.getTenantId(), payload);
        }
    }
}
