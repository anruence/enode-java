package org.enodeframework.spring;

import com.aliyun.openservices.ons.api.Producer;
import org.enodeframework.ons.message.OnsApplicationMessageListener;
import org.enodeframework.ons.message.OnsCommandListener;
import org.enodeframework.ons.message.OnsDomainEventListener;
import org.enodeframework.ons.message.OnsPublishableExceptionListener;
import org.enodeframework.ons.message.SendOnsService;
import org.enodeframework.queue.IMessageHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "ons")
public class EnodeOnsAutoConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "exception")
    public OnsPublishableExceptionListener publishableExceptionListener(@Qualifier(value = "defaultPublishableExceptionListener") IMessageHandler publishableExceptionListener) {
        return new OnsPublishableExceptionListener(publishableExceptionListener);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "application")
    public OnsApplicationMessageListener applicationMessageListener(@Qualifier(value = "defaultApplicationMessageListener") IMessageHandler applicationMessageListener) {
        return new OnsApplicationMessageListener(applicationMessageListener);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "event")
    public OnsDomainEventListener domainEventListener(@Qualifier(value = "defaultDomainEventListener") IMessageHandler domainEventListener) {
        return new OnsDomainEventListener(domainEventListener);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.mq.topic", name = "command")
    public OnsCommandListener commandListener(@Qualifier(value = "defaultCommandListener") IMessageHandler commandListener) {
        return new OnsCommandListener(commandListener);
    }

    @Bean
    public SendOnsService sendOnsService(@Qualifier(value = "enodeOnsProducer") Producer producer) {
        return new SendOnsService(producer);
    }
}
