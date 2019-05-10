package com.enode.infrastructure.impl;

import com.enode.infrastructure.IPublishableException;
import com.enode.infrastructure.ProcessingPublishableExceptionMessage;

public class DefaultPublishableExceptionProcessor extends DefaultMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException> {

    @Override
    public String getMessageName() {
        return "exception message";
    }
}

