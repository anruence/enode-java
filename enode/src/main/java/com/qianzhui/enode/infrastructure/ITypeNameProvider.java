package com.qianzhui.enode.infrastructure;

public interface ITypeNameProvider {
    String getTypeName(Class type);

    Class getType(String typeName);
}
