package com.enode.common.function;

public interface Action3<T1, T2, T3> {
    void apply(T1 obj1, T2 obj2, T3 obj3) throws Exception;
}
