package org.enodeframework.common.function;

public interface Func2<T1, T2, TResult> {
    TResult apply(T1 obj1, T2 obj2);
}
