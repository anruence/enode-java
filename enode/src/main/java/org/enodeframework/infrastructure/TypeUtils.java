package org.enodeframework.infrastructure;

import org.enodeframework.domain.IAggregateRepository;
import org.enodeframework.domain.IAggregateRoot;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

/**
 * @author anruence@gmail.com
 */
public class TypeUtils {
    public static boolean isAggregateRoot(Class type) {
        return !Modifier.isAbstract(type.getModifiers()) && IAggregateRoot.class.isAssignableFrom(type);
    }

    public static boolean isAggregateRepositoryType(Class type) {
        return type != null && !Modifier.isAbstract(type.getModifiers()) && IAggregateRepository.class.isAssignableFrom(type);
    }

    public static Class getGenericType(Class clazz) {
        return (Class) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
