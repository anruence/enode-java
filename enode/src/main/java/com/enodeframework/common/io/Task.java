package com.enodeframework.common.io;

import com.enodeframework.infrastructure.WrappedRuntimeException;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class Task extends CompletableFuture {

    public static CompletableFuture CompletedTask = CompletableFuture.completedFuture(null);

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
            return future.get();
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
