package com.enode.infrastructure.impl;

import com.enode.eventing.DomainEventStreamMessage;
import com.enode.infrastructure.ProcessingDomainEventStreamMessage;

public class DefaultDomainEventProcessor extends DefaultMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> {

    @Override
    public String getMessageName() {
        return "event message";
    }
}
