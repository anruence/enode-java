package com.qianzhui.enode.infrastructure;

import com.qianzhui.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandlerProxy2 extends IObjectProxy, MethodInvocation {
    CompletableFuture<AsyncTaskResult> handleAsync(IMessage message1, IMessage message2);
}
