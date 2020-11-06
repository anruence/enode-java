package org.enodeframework.eventing;

import java.util.concurrent.CompletableFuture;

/**
 * Represents the event processing context.
 */
public interface IEventProcessContext {
    /**
     * Notify the event has been processed.
     */
    CompletableFuture<Void> notifyEventProcessed();
}
