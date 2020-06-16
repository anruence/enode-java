package org.enodeframework.infrastructure;

public interface ITypeNameProvider {
    /**
     * 获取ClassName
     */
    String getTypeName(Class<?> type);

    /**
     * 根据name找Class
     */
    Class<?> getType(String typeName);
}
