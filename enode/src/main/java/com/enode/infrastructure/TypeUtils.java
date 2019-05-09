package com.enode.infrastructure;

import com.enode.domain.IAggregateRoot;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeUtils {

    public static boolean isAggregateRoot(Class type) {
        return !Modifier.isAbstract(type.getModifiers()) && IAggregateRoot.class.isAssignableFrom(type);
    }

    public static Type getSuperGenericInterface(Class implementerType, Class toResolve) {
        Type[] genericInterfaces = implementerType.getGenericInterfaces();

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface == toResolve) {
                return genericInterface;
            }
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType genericInterfaceType = (ParameterizedType) genericInterface;

                if (genericInterfaceType.getRawType() == toResolve) {
                    return genericInterfaceType;
                }
            }
        }

        //查找一级父类（只支持一层）
        Type genericSuperclass = implementerType.getGenericSuperclass();
        if (genericSuperclass == null) {
            return null;
        }

        if (genericSuperclass instanceof Class) {
            if (toResolve.isAssignableFrom((Class) genericSuperclass)) {
                return toResolve;
            }
            return null;
        }

        ParameterizedType genericSuperclassType = (ParameterizedType) genericSuperclass;

        Type superGenericRawType = genericSuperclassType.getRawType();

        if (!(superGenericRawType instanceof Class)) {
            return null;
        }

        Class<?>[] interfaces = ((Class) superGenericRawType).getInterfaces();
        for (Class<?> anInterface : interfaces) {
            if (anInterface == toResolve) {
                //假设父类与目标接口泛型一致
                return ParameterizedTypeImpl.make(toResolve, genericSuperclassType.getActualTypeArguments(), null);
            }
        }

        if (toResolve.isAssignableFrom((Class) superGenericRawType)) {
            return ParameterizedTypeImpl.make(toResolve, genericSuperclassType.getActualTypeArguments(), null);
        }

        return null;
    }
}
