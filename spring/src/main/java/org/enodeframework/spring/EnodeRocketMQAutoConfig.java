package org.enodeframework.spring;

import org.apache.rocketmq.client.producer.MQProducer;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.rocketmq.message.RocketMQApplicationMessageListener;
import org.enodeframework.rocketmq.message.RocketMQCommandListener;
import org.enodeframework.rocketmq.message.RocketMQDomainEventListener;
import org.enodeframework.rocketmq.message.RocketMQPublishableExceptionListener;
import org.enodeframework.rocketmq.message.SendRocketMQService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "rocketmq")
public class EnodeRocketMQAutoConfig {

    @Bean
    public RocketMQPublishableExceptionListener publishableExceptionListener(IMessageHandler publishableExceptionListener) {
        return new RocketMQPublishableExceptionListener(publishableExceptionListener);
    }

    @Bean
    public RocketMQApplicationMessageListener applicationMessageListener(IMessageHandler applicationMessageListener) {
        return new RocketMQApplicationMessageListener(applicationMessageListener);
    }

    @Bean
    public RocketMQDomainEventListener domainEventListener(IMessageHandler domainEventListener) {
        return new RocketMQDomainEventListener(domainEventListener);
    }

    @Bean
    public RocketMQCommandListener commandListener(IMessageHandler commandListener) {
        return new RocketMQCommandListener(commandListener);
    }

    @Bean
    public SendRocketMQService sendRocketMQService(MQProducer mqProducer) {
        return new SendRocketMQService(mqProducer);
    }
}
