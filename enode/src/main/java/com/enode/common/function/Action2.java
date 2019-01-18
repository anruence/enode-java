package com.enode.common.function;

public interface Action2<T1, T2> {
    void apply(T1 obj1, T2 obj2) throws Exception;
}
