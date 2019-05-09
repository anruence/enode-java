package com.enode.infrastructure.impl;

import com.enode.common.scheduling.IScheduleService;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IProcessingMessageHandler;
import com.enode.infrastructure.IProcessingMessageScheduler;
import com.enode.infrastructure.ProcessingApplicationMessage;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
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
