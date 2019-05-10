package com.enode.infrastructure.impl;

import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.ProcessingApplicationMessage;

public class DefaultApplicationMessageProcessor extends DefaultMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> {

    @Override
    public String getMessageName() {
        return "application message";
    }
}
