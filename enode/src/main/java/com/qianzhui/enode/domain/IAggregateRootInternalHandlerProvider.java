package com.qianzhui.enode.domain;

import com.qianzhui.enode.common.function.Action2;
import com.qianzhui.enode.eventing.IDomainEvent;

/**
 * Defines a provider interface to provide the aggregate root internal handler.
 */
public interface IAggregateRootInternalHandlerProvider {

    /**
     * Get the internal event handler within the aggregate.
     *
     * @param aggregateRootType
     * @param anEventType
     * @return
     */
    Action2<IAggregateRoot, IDomainEvent> getInternalEventHandler(Class<? extends IAggregateRoot> aggregateRootType, Class<? extends IDomainEvent> anEventType);
}
