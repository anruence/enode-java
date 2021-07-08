package org.enodeframework.infrastructure

import org.enodeframework.domain.IAggregateRepository
import org.enodeframework.domain.IAggregateRoot
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 * @author anruence@gmail.com
 */
object TypeUtils {
    fun isAggregateRoot(type: Class<*>): Boolean {
        return !Modifier.isAbstract(type.modifiers) && IAggregateRoot::class.java.isAssignableFrom(type)
    }

    fun isAggregateRepositoryType(type: Class<*>): Boolean {
        return !Modifier.isAbstract(type.modifiers) && IAggregateRepository::class.java.isAssignableFrom(type)
    }

    fun getGenericType(clazz: Class<*>): Class<*> {
        return (clazz.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
    }
}