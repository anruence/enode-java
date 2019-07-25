package com.enodeframework.infrastructure.impl;

import com.enodeframework.infrastructure.IPublishableException;
import com.enodeframework.infrastructure.ProcessingPublishableExceptionMessage;

/**
 * @author anruence@gmail.com
 */
public class DefaultPublishableExceptionProcessor extends DefaultMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException> {
    @Override
    public String getMessageName() {
        return "exception message";
    }
}
