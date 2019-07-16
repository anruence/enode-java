package org.enodeframework.messaging;

import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandlerProxy1 extends IObjectProxy, MethodInvocation {
    CompletableFuture<Void> handleAsync(IMessage message);
}
