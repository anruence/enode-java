package org.enodeframework.messaging.impl;

import org.enodeframework.messaging.IMessage;
import org.enodeframework.messaging.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DoNothingPublisher<TMessage extends IMessage> implements IMessagePublisher<TMessage> {
    private static final CompletableFuture<Void> SUCCESSRESULTTASK = CompletableFuture.completedFuture(null);

    @Override
    public CompletableFuture<Void> publishAsync(TMessage message) {
        return SUCCESSRESULTTASK;
    }
}
