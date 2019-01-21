package com.enode.rocketmq;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessage;

import java.util.concurrent.CompletableFuture;

public interface IMQProducer {

    CompletableFuture<AsyncTaskResult> sendAsync(final IMessage msg, final String routingKey);

    CompletableFuture<AsyncTaskResult> sendAsync(final IMessage msg, final String routingKey, boolean sendReply);
}
