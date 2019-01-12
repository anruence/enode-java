package com.qianzhui.enode.common.function;

public interface Func<TResult> {
    TResult apply() throws Exception;
}
