package org.enodeframework.spring;

import com.aliyun.openservices.ons.api.Producer;
import org.enodeframework.ons.message.OnsMessageListener;
import org.enodeframework.ons.message.OnsSendMessageService;
import org.enodeframework.queue.IMessageHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "ons")
public class EnodeOnsAutoConfig {

    @Bean(name = "onsPublishableExceptionListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "exception")
    public OnsMessageListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionListener") IMessageHandler publishableExceptionListener) {
        return new OnsMessageListener(publishableExceptionListener);
    }

    @Bean(name = "onsApplicationMessageListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "application")
    public OnsMessageListener applicationMessageListener(@Qualifier(value = "defaultApplicationMessageListener") IMessageHandler applicationMessageListener) {
        return new OnsMessageListener(applicationMessageListener);
    }

    @Bean(name = "onsDomainEventListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public OnsMessageListener domainEventListener(@Qualifier(value = "defaultDomainEventListener") IMessageHandler domainEventListener) {
        return new OnsMessageListener(domainEventListener);
    }

    @Bean(name = "onsCommandListener")
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public OnsMessageListener commandListener(@Qualifier(value = "defaultCommandListener") IMessageHandler commandListener) {
        return new OnsMessageListener(commandListener);
    }

    @Bean(name = "onsSendMessageService")
    public OnsSendMessageService onsSendMessageService(@Qualifier(value = "enodeOnsProducer") Producer producer) {
        return new OnsSendMessageService(producer);
    }
}
