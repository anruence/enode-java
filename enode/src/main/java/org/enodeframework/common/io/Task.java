package org.enodeframework.common.io;

import io.vertx.ext.sync.Sync;
import org.enodeframework.common.exception.ENodeInterruptException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class Task {

    public static CompletableFuture<Void> completedTask = CompletableFuture.completedFuture(null);

    public static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new ENodeInterruptException(e);
        }
    }

    public static <T> T await(CompletableFuture<T> future) {
        return future.join();
    }

    public static void sleep(long sleepMilliseconds) {
        try {
            Thread.sleep(sleepMilliseconds);
        } catch (InterruptedException e) {
            throw new ENodeInterruptException(e);
        }
    }
}
