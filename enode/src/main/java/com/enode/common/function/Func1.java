package com.enode.common.function;

public interface Func1<T, TResult> {
    TResult apply(T obj) throws Exception;
}
