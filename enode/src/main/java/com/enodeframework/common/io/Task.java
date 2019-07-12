package com.enodeframework.common.io;

import com.enodeframework.infrastructure.WrappedRuntimeException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author anruence@gmail.com
 */
public class Task extends CompletableFuture {

    public static CompletableFuture CompletedTask = CompletableFuture.completedFuture(null);

    public static <T> CompletableFuture<T> fromResult(T o) {
        return Task.completedFuture(o);
    }

    /**
     * async await operation
     *
     * @param future
     * @param <T>
     * @return
     */
    public static <T> T await(CompletableFuture<T> future) {
        return future.join();
    }

    public static <T> T get(CompletableFuture<T> future) {
        try {
            return future.get(5000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    public static void sleep(long sleepMilliseconds) {
        try {
            Thread.sleep(sleepMilliseconds);
        } catch (InterruptedException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
