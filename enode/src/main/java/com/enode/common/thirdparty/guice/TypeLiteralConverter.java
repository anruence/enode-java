package com.enode.common.thirdparty.guice;

import com.enode.common.container.GenericTypeLiteral;
import com.google.inject.TypeLiteral;

import java.lang.reflect.Type;

public class TypeLiteralConverter {

    public static <T> TypeLiteral<T> convert(GenericTypeLiteral<T> genericTypeLiteral) {
        Type superclassTypeParameter = genericTypeLiteral.getType();
        TypeLiteral<?> typeLiteral = TypeLiteral.get(superclassTypeParameter);
        return (TypeLiteral<T>) TypeLiteral.get(superclassTypeParameter);
    }
}
