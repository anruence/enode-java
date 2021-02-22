package org.enodeframework.spring;

import org.apache.pulsar.client.api.Producer;
import org.enode.pulsar.message.PulsarMessageListener;
import org.enode.pulsar.message.PulsarSendMessageService;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "pulsar")
public class EnodePulsarAutoConfig {

    @Bean(name = "pulsarPublishableExceptionListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "exception")
    public PulsarMessageListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionListener") IMessageHandler publishableExceptionListener) {
        return new PulsarMessageListener(publishableExceptionListener);
    }

    @Bean(name = "pulsarApplicationMessageListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "application")
    public PulsarMessageListener applicationMessageListener(@Qualifier(value = "defaultApplicationMessageListener") IMessageHandler applicationMessageListener) {
        return new PulsarMessageListener(applicationMessageListener);
    }

    @Bean(name = "pulsarDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public PulsarMessageListener domainEventListener(@Qualifier(value = "defaultDomainEventListener") IMessageHandler domainEventListener) {
        return new PulsarMessageListener(domainEventListener);
    }

    @Bean(name = "pulsarCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public PulsarMessageListener commandListener(@Qualifier(value = "defaultCommandListener") IMessageHandler commandListener) {
        return new PulsarMessageListener(commandListener);
    }

    @Bean(name = "pulsarSendMessageService")
    public PulsarSendMessageService pulsarSendMessageService(@Qualifier(value = "enodePulsarProducer") Producer<QueueMessage> producer) {
        return new PulsarSendMessageService(producer);
    }
}
