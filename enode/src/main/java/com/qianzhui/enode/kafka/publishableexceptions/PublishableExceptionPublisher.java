package com.qianzhui.enode.kafka.publishableexceptions;

import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.infrastructure.IMessagePublisher;
import com.qianzhui.enode.infrastructure.IPublishableException;

import java.util.concurrent.CompletableFuture;

public class PublishableExceptionPublisher implements IMessagePublisher<IPublishableException> {

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException message) {
        return null;
    }
}
