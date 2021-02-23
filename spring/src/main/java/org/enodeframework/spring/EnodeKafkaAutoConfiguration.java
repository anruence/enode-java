package org.enodeframework.spring;

import org.enodeframework.kafka.KafkaMessageListener;
import org.enodeframework.kafka.KafkaSendMessageService;
import org.enodeframework.queue.IMessageHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "kafka")
public class EnodeKafkaAutoConfiguration {

    @Bean(name = "kafkaPublishableExceptionListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "exception")
    public KafkaMessageListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionMessageHandler") IMessageHandler publishableExceptionListener) {
        return new KafkaMessageListener(publishableExceptionListener);
    }

    @Bean(name = "kafkaApplicationMessageListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "application")
    public KafkaMessageListener applicationMessageListener(@Qualifier(value = "defaultApplicationMessageHandler") IMessageHandler applicationMessageListener) {
        return new KafkaMessageListener(applicationMessageListener);
    }

    @Bean(name = "kafkaDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public KafkaMessageListener domainEventListener(@Qualifier(value = "defaultDomainEventMessageHandler") IMessageHandler domainEventListener) {
        return new KafkaMessageListener(domainEventListener);
    }

    @Bean(name = "kafkaCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public KafkaMessageListener commandListener(@Qualifier(value = "defaultCommandMessageHandler") IMessageHandler commandListener) {
        return new KafkaMessageListener(commandListener);
    }

    @Bean(name = "kafkaSendMessageService")
    public KafkaSendMessageService kafkaSendMessageService(@Qualifier(value = "enodeKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaSendMessageService(kafkaTemplate);
    }
}