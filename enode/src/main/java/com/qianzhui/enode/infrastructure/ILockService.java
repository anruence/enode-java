package com.qianzhui.enode.infrastructure;

import com.qianzhui.enode.common.function.Action;

public interface ILockService {
    void addLockKey(String lockKey);

    void executeInLock(String lockKey, Action action);
}
