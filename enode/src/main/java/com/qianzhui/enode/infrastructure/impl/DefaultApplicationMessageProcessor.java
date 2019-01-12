package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.common.scheduling.IScheduleService;
import com.qianzhui.enode.infrastructure.IApplicationMessage;
import com.qianzhui.enode.infrastructure.IProcessingMessageHandler;
import com.qianzhui.enode.infrastructure.IProcessingMessageScheduler;
import com.qianzhui.enode.infrastructure.ProcessingApplicationMessage;

import javax.inject.Inject;


public class DefaultApplicationMessageProcessor extends DefaultMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> {

    @Inject
    public DefaultApplicationMessageProcessor(
            IProcessingMessageScheduler<ProcessingApplicationMessage, IApplicationMessage> processingMessageScheduler,
            IProcessingMessageHandler<ProcessingApplicationMessage, IApplicationMessage> processingMessageHandler,
            IScheduleService scheduleService) {
        super(processingMessageScheduler, processingMessageHandler, scheduleService);
    }

    @Override
    public String getMessageName() {
        return "application message";
    }
}
