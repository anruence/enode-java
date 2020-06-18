package org.enodeframework.kafka;

import org.enodeframework.queue.IMessageHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

public class EnodeKafkaAutoConfiguration {

    @Bean
    public KafkaPublishableExceptionListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionListener") IMessageHandler publishableExceptionListener) {
        return new KafkaPublishableExceptionListener(publishableExceptionListener);
    }

    @Bean
    public KafkaApplicationMessageListener applicationMessageListener(@Qualifier(value = "defaultApplicationMessageListener") IMessageHandler applicationMessageListener) {
        return new KafkaApplicationMessageListener(applicationMessageListener);
    }

    @Bean
    public KafkaDomainEventListener domainEventListener(@Qualifier(value = "defaultDomainEventListener") IMessageHandler domainEventListener) {
        return new KafkaDomainEventListener(domainEventListener);
    }

    @Bean
    public KafkaCommandListener commandListener(@Qualifier(value = "defaultCommandListener") IMessageHandler commandListener) {
        return new KafkaCommandListener(commandListener);
    }

    @Bean
    public SendKafkaMessageService sendKafkaMessageService(KafkaTemplate<String, String> kafkaTemplate) {
        return new SendKafkaMessageService(kafkaTemplate);
    }
}