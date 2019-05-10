package com.enode.infrastructure.impl;

import com.enode.common.extensions.ENodeException;
import com.enode.infrastructure.ITypeNameProvider;

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
            throw new ENodeException("ClassNotFound", e);
        }
    }
}
