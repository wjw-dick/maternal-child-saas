package com.quantaida.mc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑：业务队列 / 通知队列 / 审计队列 + TTL 延迟队列。
 *
 * <p>仅当 {@code app.mq.enabled=true} 时生效；本地演示默认关闭，EventBus 自动降级为日志。</p>
 */
@Configuration
@ConditionalOnProperty(name = "app.mq.enabled", havingValue = "true")
public class RabbitConfig {
    public static final String EXCHANGE = "mc.topic";
    public static final String DLX = "mc.dlx";
    public static final String BIZ_QUEUE = "mc.biz.queue";
    public static final String NOTIFY_QUEUE = "mc.notify.queue";
    public static final String AUDIT_QUEUE = "mc.audit.queue";
    public static final String DELAY_QUEUE = "mc.delay.queue";

    @Bean
    public TopicExchange mcTopicExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange mcDeadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue mcBizQueue() {
        return QueueBuilder.durable(BIZ_QUEUE)
                .deadLetterExchange(DLX).deadLetterRoutingKey("mc.biz.dead")
                .build();
    }

    @Bean
    public Queue mcNotifyQueue() {
        return QueueBuilder.durable(NOTIFY_QUEUE)
                .deadLetterExchange(DLX).deadLetterRoutingKey("mc.notify.dead")
                .build();
    }

    @Bean
    public Queue mcAuditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE)
                .deadLetterExchange(DLX).deadLetterRoutingKey("mc.audit.dead")
                .build();
    }

    /**
     * 延迟队列：消息过期后进入死信交换机，再路由到通知队列。
     */
    @Bean
    public Queue mcDelayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .deadLetterExchange(DLX).deadLetterRoutingKey("mc.notify.delay")
                .build();
    }

    @Bean
    public Queue mcDeadQueue() {
        return QueueBuilder.durable("mc.dead.queue").build();
    }

    @Bean
    public Binding bizBinding(Queue mcBizQueue, TopicExchange mcTopicExchange) {
        return BindingBuilder.bind(mcBizQueue).to(mcTopicExchange).with("mc.order.#");
    }

    @Bean
    public Binding appointmentBinding(Queue mcBizQueue, TopicExchange mcTopicExchange) {
        return BindingBuilder.bind(mcBizQueue).to(mcTopicExchange).with("mc.appointment.#");
    }

    @Bean
    public Binding notifyBinding(Queue mcNotifyQueue, TopicExchange mcTopicExchange) {
        return BindingBuilder.bind(mcNotifyQueue).to(mcTopicExchange).with("mc.notify.#");
    }

    @Bean
    public Binding auditBinding(Queue mcAuditQueue, TopicExchange mcTopicExchange) {
        return BindingBuilder.bind(mcAuditQueue).to(mcTopicExchange).with("mc.audit.#");
    }

    @Bean
    public Binding delayBinding(Queue mcNotifyQueue, DirectExchange mcDeadLetterExchange) {
        return BindingBuilder.bind(mcNotifyQueue).to(mcDeadLetterExchange).with("mc.notify.delay");
    }

    @Bean
    public Binding deadBinding(Queue mcDeadQueue, DirectExchange mcDeadLetterExchange) {
        return BindingBuilder.bind(mcDeadQueue).to(mcDeadLetterExchange).with("mc.biz.dead");
    }

    @Bean
    public Binding deadNotifyBinding(Queue mcDeadQueue, DirectExchange mcDeadLetterExchange) {
        return BindingBuilder.bind(mcDeadQueue).to(mcDeadLetterExchange).with("mc.notify.dead");
    }

    @Bean
    public Binding deadAuditBinding(Queue mcDeadQueue, DirectExchange mcDeadLetterExchange) {
        return BindingBuilder.bind(mcDeadQueue).to(mcDeadLetterExchange).with("mc.audit.dead");
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        return template;
    }
}
