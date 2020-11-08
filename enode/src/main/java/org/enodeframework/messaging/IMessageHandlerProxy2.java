package org.enodeframework.messaging;

import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandlerProxy2 extends IObjectProxy, MethodInvocation {
    CompletableFuture<Boolean> handleAsync(IMessage message1, IMessage message2);
}
