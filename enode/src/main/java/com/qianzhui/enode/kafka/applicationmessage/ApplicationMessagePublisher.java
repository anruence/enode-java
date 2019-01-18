package com.qianzhui.enode.kafka.applicationmessage;

import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.infrastructure.IApplicationMessage;
import com.qianzhui.enode.infrastructure.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

public class ApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        return null;
    }
}
