package com.enode.domain.impl;

import com.enode.domain.IAggregateRoot;
import com.enode.domain.IAggregateRootFactory;

public class DefaultAggregateRootFactory implements IAggregateRootFactory {

    @Override
    public <T extends IAggregateRoot> T createAggregateRoot(Class<T> aggregateRootType) {
        try {
            return aggregateRootType.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
