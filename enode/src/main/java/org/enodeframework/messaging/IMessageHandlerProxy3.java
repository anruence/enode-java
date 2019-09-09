package org.enodeframework.messaging;

import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandlerProxy3 extends IObjectProxy, MethodInvocation {
    CompletableFuture<AsyncTaskResult> handleAsync(IMessage message1, IMessage message2, IMessage message3);
}
