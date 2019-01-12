package com.qianzhui.enode.common.function;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.time.Duration;
import java.util.concurrent.*;

public class DelayedTask {
    private static final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("DelayedThread-%d").build());

    public static <T> CompletableFuture<T> startDelayedTaskFuture(Duration duration, Func<T> action) {
        CompletableFuture promise = new CompletableFuture();
        schedule.schedule(() -> promise.complete(action.apply()), duration.toMillis(), TimeUnit.MILLISECONDS);
        return promise;
    }

    public static void startDelayedTask(Duration duration, Action action) {
        DelayedTask.schedule.schedule(() -> {
            try {
                action.apply();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        CompletableFuture<String> instance = DelayedTask.startDelayedTaskFuture(Duration.ofSeconds(1), () -> {
            return "test";
        });

        instance.thenAccept(x -> {
            System.out.println(x);
        });

        Thread.sleep(2000);
    }
}
