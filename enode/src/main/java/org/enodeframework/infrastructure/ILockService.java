package org.enodeframework.infrastructure;

import org.enodeframework.common.function.Action;

/**
 * 定义一个用于实现锁的接口，暂时不需要
 */
public interface ILockService {
    void addLockKey(String lockKey);

    void executeInLock(String lockKey, Action action);
}
