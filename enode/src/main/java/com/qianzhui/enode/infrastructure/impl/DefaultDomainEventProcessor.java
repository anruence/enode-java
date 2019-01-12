package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.common.scheduling.IScheduleService;
import com.qianzhui.enode.eventing.DomainEventStreamMessage;
import com.qianzhui.enode.infrastructure.IProcessingMessageHandler;
import com.qianzhui.enode.infrastructure.IProcessingMessageScheduler;
import com.qianzhui.enode.infrastructure.ProcessingDomainEventStreamMessage;

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
