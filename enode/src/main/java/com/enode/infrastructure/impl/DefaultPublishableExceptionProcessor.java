package com.enode.infrastructure.impl;

import com.enode.common.scheduling.IScheduleService;
import com.enode.infrastructure.IProcessingMessageHandler;
import com.enode.infrastructure.IProcessingMessageScheduler;
import com.enode.infrastructure.IPublishableException;
import com.enode.infrastructure.ProcessingPublishableExceptionMessage;

import javax.inject.Inject;

public class DefaultPublishableExceptionProcessor extends DefaultMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException> {
    @Inject
    public DefaultPublishableExceptionProcessor(
            IProcessingMessageScheduler<ProcessingPublishableExceptionMessage, IPublishableException> processingMessageScheduler,
            IProcessingMessageHandler<ProcessingPublishableExceptionMessage, IPublishableException> processingMessageHandler,
            IScheduleService scheduleService) {
        super(processingMessageScheduler, processingMessageHandler, scheduleService);
    }

    @Override
    public String getMessageName() {
        return "exception message";
    }
}

