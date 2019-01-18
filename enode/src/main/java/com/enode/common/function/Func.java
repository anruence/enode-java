package com.enode.common.function;

public interface Func<TResult> {
    TResult apply() throws Exception;
}
