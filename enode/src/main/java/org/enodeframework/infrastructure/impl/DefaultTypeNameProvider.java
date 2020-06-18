package org.enodeframework.infrastructure.impl;

import org.enodeframework.common.exception.EnodeRuntimeException;
import org.enodeframework.infrastructure.ITypeNameProvider;

/**
 * @author anruence@gmail.com
 */
public class DefaultTypeNameProvider implements ITypeNameProvider {
    @Override
    public String getTypeName(Class<?> type) {
        return type.getName();
    }

    @Override
    public Class<?> getType(String typeName) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new EnodeRuntimeException(e);
        }
    }
}
