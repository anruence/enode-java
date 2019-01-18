package com.enode.infrastructure.impl;

import com.enode.common.scheduling.IScheduleService;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.infrastructure.IProcessingMessageHandler;
import com.enode.infrastructure.IProcessingMessageScheduler;
import com.enode.infrastructure.ProcessingDomainEventStreamMessage;

import javax.inject.Inject;

public class DefaultDomainEventProcessor extends DefaultMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> {
    @Inject
    public DefaultDomainEventProcessor(
            IProcessingMessageScheduler<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> processingMessageScheduler,
            IProcessingMessageHandler<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> processingMessageHandler,
            IScheduleService scheduleService) {
        super(processingMessageScheduler, processingMessageHandler, scheduleService);
    }

    @Override
    public String getMessageName() {
        return "event message";
    }
}
