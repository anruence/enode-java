package org.enodeframework.spring;

import org.apache.rocketmq.client.producer.MQProducer;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.rocketmq.message.RocketMQApplicationMessageListener;
import org.enodeframework.rocketmq.message.RocketMQCommandListener;
import org.enodeframework.rocketmq.message.RocketMQDomainEventListener;
import org.enodeframework.rocketmq.message.RocketMQPublishableExceptionListener;
import org.enodeframework.rocketmq.message.SendRocketMQService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "rocketmq")
public class EnodeRocketMQAutoConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "exception")
    public RocketMQPublishableExceptionListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionListener") IMessageHandler publishableExceptionListener) {
        return new RocketMQPublishableExceptionListener(publishableExceptionListener);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "application")
    public RocketMQApplicationMessageListener applicationMessageListener(@Qualifier(value = "defaultApplicationMessageListener") IMessageHandler applicationMessageListener) {
        return new RocketMQApplicationMessageListener(applicationMessageListener);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public RocketMQDomainEventListener domainEventListener(@Qualifier(value = "defaultDomainEventListener") IMessageHandler domainEventListener) {
        return new RocketMQDomainEventListener(domainEventListener);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public RocketMQCommandListener commandListener(@Qualifier(value = "defaultCommandListener") IMessageHandler commandListener) {
        return new RocketMQCommandListener(commandListener);
    }

    @Bean
    public SendRocketMQService sendRocketMQService(@Qualifier(value = "enodeMQProducer") MQProducer mqProducer) {
        return new SendRocketMQService(mqProducer);
    }
}
