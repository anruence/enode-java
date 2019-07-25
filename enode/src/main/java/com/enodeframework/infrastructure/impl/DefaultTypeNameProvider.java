package com.enodeframework.infrastructure.impl;

import com.enodeframework.common.exception.ENodeRuntimeException;
import com.enodeframework.infrastructure.ITypeNameProvider;

/**
 * @author anruence@gmail.com
 */
public class DefaultTypeNameProvider implements ITypeNameProvider {
    @Override
    public String getTypeName(Class type) {
        return type.getName();
    }

    @Override
    public Class getType(String typeName) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new ENodeRuntimeException("ClassNotFound", e);
        }
    }
}
