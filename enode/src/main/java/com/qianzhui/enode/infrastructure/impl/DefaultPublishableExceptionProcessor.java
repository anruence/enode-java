package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.common.scheduling.IScheduleService;
import com.qianzhui.enode.infrastructure.IProcessingMessageHandler;
import com.qianzhui.enode.infrastructure.IProcessingMessageScheduler;
import com.qianzhui.enode.infrastructure.IPublishableException;
import com.qianzhui.enode.infrastructure.ProcessingPublishableExceptionMessage;

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

