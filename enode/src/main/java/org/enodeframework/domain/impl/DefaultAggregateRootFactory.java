package org.enodeframework.domain.impl;

import org.enodeframework.common.exception.AggregateRootCreateException;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IAggregateRootFactory;

/**
 * @author anruence@gmail.com
 */
public class DefaultAggregateRootFactory implements IAggregateRootFactory {
    @Override
    public <T extends IAggregateRoot> T createAggregateRoot(Class<T> aggregateRootType) {
        try {
            return aggregateRootType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new AggregateRootCreateException(e);
        }
    }
}
