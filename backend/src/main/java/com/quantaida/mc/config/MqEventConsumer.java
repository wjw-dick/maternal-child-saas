package com.quantaida.mc.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 异步事件消费者：手动 ACK，失败进入死信队列，单条消息异常不影响队列后续消费。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mq.enabled", havingValue = "true")
public class MqEventConsumer {
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.BIZ_QUEUE)
    public void onBizEvent(Message message, Channel channel) throws IOException {
        consume(message, channel, "BIZ");
    }

    @RabbitListener(queues = RabbitConfig.NOTIFY_QUEUE)
    public void onNotifyEvent(Message message, Channel channel) throws IOException {
        consume(message, channel, "NOTIFY");
    }

    @RabbitListener(queues = RabbitConfig.AUDIT_QUEUE)
    public void onAuditEvent(Message message, Channel channel) throws IOException {
        consume(message, channel, "AUDIT");
    }

    @RabbitListener(queues = RabbitConfig.DELAY_QUEUE)
    public void onDelayEvent(Message message, Channel channel) throws IOException {
        consume(message, channel, "DELAY");
    }

    @RabbitListener(queues = "mc.dead.queue")
    public void onDeadEvent(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        log.error("[MQ-DEAD] {}", new String(message.getBody(), StandardCharsets.UTF_8));
        channel.basicAck(tag, false);
    }

    private void consume(Message message, Channel channel, String queue) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(body);
            log.info("[MQ-{}] tenant={} type={} eventId={}", queue,
                    node.path("tenantId").asLong(), node.path("type").asText(), node.path("eventId").asText());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.warn("[MQ-{}] 消费失败，转死信: {}", queue, e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }
}
