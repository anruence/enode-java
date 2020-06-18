package org.enodeframework.rocketmq.message;

import org.enodeframework.queue.IMessageHandler;

public class EnodeRocketMQAutoConfig {

    public RocketMQPublishableExceptionListener publishableExceptionListener(IMessageHandler publishableExceptionListener) {
        return new RocketMQPublishableExceptionListener(publishableExceptionListener);
    }

    public RocketMQApplicationMessageListener applicationMessageListener(IMessageHandler applicationMessageListener) {
        return new RocketMQApplicationMessageListener(applicationMessageListener);
    }

    public RocketMQDomainEventListener domainEventListener(IMessageHandler domainEventListener) {
        return new RocketMQDomainEventListener(domainEventListener);
    }

    public RocketMQCommandListener commandListener(IMessageHandler commandListener) {
        return new RocketMQCommandListener(commandListener);
    }
}
