package com.enode.infrastructure;

import com.enode.common.function.Action;

public interface ILockService {
    void addLockKey(String lockKey);

    void executeInLock(String lockKey, Action action);
}
