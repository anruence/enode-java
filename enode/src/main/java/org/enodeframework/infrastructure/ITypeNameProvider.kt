package org.enodeframework.infrastructure

interface ITypeNameProvider {
    /**
     * 获取ClassName
     */
    fun getTypeName(type: Class<*>): String

    /**
     * 根据name找Class
     */
    fun getType(typeName: String): Class<*>
}