package com.enode.common.thirdparty.guice;

import com.google.inject.TypeLiteral;
import com.enode.commanding.ICommand;
import com.enode.common.container.GenericTypeLiteral;
import com.enode.rocketmq.ITopicProvider;

import java.lang.reflect.Type;

public class TypeLiteralConverter {
    public static <T> TypeLiteral<T> convert(GenericTypeLiteral<T> genericTypeLiteral) {
        Type superclassTypeParameter = genericTypeLiteral.getType();
//        Class<T> t = (Class<T>)superclassTypeParameter;
        TypeLiteral<?> typeLiteral = TypeLiteral.get(superclassTypeParameter);

        return (TypeLiteral<T>) TypeLiteral.get(superclassTypeParameter);
    }

    public static void main(String[] args) {
        TypeLiteral<ITopicProvider<ICommand>> t1 = TypeLiteralConverter.convert(new GenericTypeLiteral<ITopicProvider<ICommand>>() {
        });

        TypeLiteral<ITopicProvider<ICommand>> t2 = new TypeLiteral<ITopicProvider<ICommand>>() {
        };

        System.out.println(t1.getRawType());
        System.out.println(t2.getRawType());
        System.out.println(t2.equals(t1));
    }
}
