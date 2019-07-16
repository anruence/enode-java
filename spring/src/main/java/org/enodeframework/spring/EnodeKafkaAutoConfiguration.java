package org.enodeframework.spring;

import org.enodeframework.kafka.KafkaApplicationMessageListener;
import org.enodeframework.kafka.KafkaCommandListener;
import org.enodeframework.kafka.KafkaDomainEventListener;
import org.enodeframework.kafka.KafkaPublishableExceptionListener;
import org.enodeframework.kafka.SendKafkaMessageService;
import org.enodeframework.queue.IMessageHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "kafka")
public class EnodeKafkaAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "exception")
    public KafkaPublishableExceptionListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionListener") IMessageHandler publishableExceptionListener) {
        return new KafkaPublishableExceptionListener(publishableExceptionListener);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "application")
    public KafkaApplicationMessageListener applicationMessageListener(@Qualifier(value = "defaultApplicationMessageListener") IMessageHandler applicationMessageListener) {
        return new KafkaApplicationMessageListener(applicationMessageListener);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public KafkaDomainEventListener domainEventListener(@Qualifier(value = "defaultDomainEventListener") IMessageHandler domainEventListener) {
        return new KafkaDomainEventListener(domainEventListener);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public KafkaCommandListener commandListener(@Qualifier(value = "defaultCommandListener") IMessageHandler commandListener) {
        return new KafkaCommandListener(commandListener);
    }

    @Bean
    public SendKafkaMessageService sendKafkaMessageService(@Qualifier(value = "enodeKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        return new SendKafkaMessageService(kafkaTemplate);
    }
}