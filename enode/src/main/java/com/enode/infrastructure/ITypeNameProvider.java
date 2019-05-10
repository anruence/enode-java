package com.enode.infrastructure;

public interface ITypeNameProvider {
    /**
     * 获取ClassName
     *
     * @param type
     * @return
     */
    String getTypeName(Class type);

    /**
     * 跟进name找Class
     *
     * @param typeName
     * @return
     */
    Class getType(String typeName);
}
