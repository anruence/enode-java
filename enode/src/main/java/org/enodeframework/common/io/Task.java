package org.enodeframework.common.io;

import com.ea.async.Async;
import org.enodeframework.common.exception.ENodeRuntimeException;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class Task extends CompletableFuture {
    public static CompletableFuture<Void> completedTask = CompletableFuture.completedFuture(null);

    public static <T> CompletableFuture<T> fromResult(T o) {
        return Task.completedFuture(o);
    }

    /**
     * async await operation
     *
     * @param future
     * @return
     */
    public static <T> CompletableFuture<T> handle(CompletableFuture<T> future) {
        return CompletableFuture.completedFuture(Async.await(future));
    }

    public static <T> T await(CompletableFuture<T> future) {
        return handle(future).join();
    }

    public static void sleep(long sleepMilliseconds) {
        try {
            Thread.sleep(sleepMilliseconds);
        } catch (InterruptedException e) {
            throw new ENodeRuntimeException(e);
        }
    }
}
