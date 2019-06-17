package com.enodeframework.infrastructure.impl;

import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.infrastructure.ProcessingApplicationMessage;

/**
 * @author anruence@gmail.com
 */
public class DefaultApplicationMessageProcessor extends DefaultMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> {

    @Override
    public String getMessageName() {
        return "application message";
    }
}
