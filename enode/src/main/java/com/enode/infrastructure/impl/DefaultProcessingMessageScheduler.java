package com.enode.infrastructure.impl;

import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IProcessingMessage;
import com.enode.infrastructure.IProcessingMessageHandler;
import com.enode.infrastructure.IProcessingMessageScheduler;
import com.enode.infrastructure.ProcessingMessageMailbox;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class DefaultProcessingMessageScheduler<X extends IProcessingMessage<X, Y>, Y extends IMessage> implements IProcessingMessageScheduler<X, Y> {

    @Autowired
    private IProcessingMessageHandler<X, Y> _messageHandler;

    @Override
    public void scheduleMessage(X processingMessage) {
        _messageHandler.handleAsync(processingMessage);
    }

    @Override
    public void scheduleMailbox(ProcessingMessageMailbox<X, Y> mailbox) {
        CompletableFuture.runAsync(mailbox::run);
    }
}
