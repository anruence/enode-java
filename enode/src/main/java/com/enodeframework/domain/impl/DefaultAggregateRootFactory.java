package com.enodeframework.domain.impl;

import com.enodeframework.common.exception.ENodeRuntimeException;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.domain.IAggregateRootFactory;

/**
 * @author anruence@gmail.com
 */
public class DefaultAggregateRootFactory implements IAggregateRootFactory {
    @Override
    public <T extends IAggregateRoot> T createAggregateRoot(Class<T> aggregateRootType) {
        try {
            return aggregateRootType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ENodeRuntimeException(e);
        }
    }
}
