package com.qianzhui.enode.domain.impl;

import com.qianzhui.enode.domain.IAggregateRoot;
import com.qianzhui.enode.domain.IAggregateRootFactory;

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
