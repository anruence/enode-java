package org.enodeframework.messaging.impl;

import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.messaging.IMessage;
import org.enodeframework.messaging.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DoNothingPublisher<TMessage extends IMessage> implements IMessagePublisher<TMessage> {
    private static final CompletableFuture<AsyncTaskResult> SUCCESSRESULTTASK = CompletableFuture.completedFuture(AsyncTaskResult.Success);

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(TMessage message) {
        return SUCCESSRESULTTASK;
    }
}
