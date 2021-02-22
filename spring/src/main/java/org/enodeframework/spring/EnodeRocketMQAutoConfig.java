package org.enodeframework.spring;

import org.apache.rocketmq.client.producer.MQProducer;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.rocketmq.message.RocketMQMessageListener;
import org.enodeframework.rocketmq.message.RocketMQSendMessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "rocketmq")
public class EnodeRocketMQAutoConfig {

    @Bean(name = "rocketMQPublishableExceptionListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "exception")
    public RocketMQMessageListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionListener") IMessageHandler publishableExceptionListener) {
        return new RocketMQMessageListener(publishableExceptionListener);
    }

    @Bean(name = "rocketMQApplicationMessageListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "application")
    public RocketMQMessageListener applicationMessageListener(@Qualifier(value = "defaultApplicationMessageListener") IMessageHandler applicationMessageListener) {
        return new RocketMQMessageListener(applicationMessageListener);
    }

    @Bean(name = "rocketMQDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public RocketMQMessageListener domainEventListener(@Qualifier(value = "defaultDomainEventListener") IMessageHandler domainEventListener) {
        return new RocketMQMessageListener(domainEventListener);
    }

    @Bean(name = "rocketMQCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public RocketMQMessageListener commandListener(@Qualifier(value = "defaultCommandListener") IMessageHandler commandListener) {
        return new RocketMQMessageListener(commandListener);
    }

    @Bean(name = "rocketMQSendMessageService")
    public RocketMQSendMessageService rocketMQSendMessageService(@Qualifier(value = "enodeMQProducer") MQProducer mqProducer) {
        return new RocketMQSendMessageService(mqProducer);
    }
}
