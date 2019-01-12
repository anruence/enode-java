package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.infrastructure.ITypeNameProvider;

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
            throw new RuntimeException(e);
        }
    }
}
