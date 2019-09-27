package org.enodeframework.infrastructure;

public interface ITypeNameProvider {
    /**
     * 获取ClassName
     *
     * @param type
     * @return
     */
    String getTypeName(Class type);

    /**
     * 根据name找Class
     *
     * @param typeName
     * @return
     */
    Class getType(String typeName);
}
