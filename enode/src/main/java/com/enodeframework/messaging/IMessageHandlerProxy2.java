package com.enodeframework.messaging;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IObjectProxy;
import com.enodeframework.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandlerProxy2 extends IObjectProxy, MethodInvocation {
    CompletableFuture<AsyncTaskResult> handleAsync(IMessage message1, IMessage message2);
}
