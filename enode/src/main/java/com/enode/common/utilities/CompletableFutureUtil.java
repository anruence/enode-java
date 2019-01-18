package com.enode.common.utilities;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class CompletableFutureUtil {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            1,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("failAfter-%d")
                    .build());

    private static <T> CompletableFuture<T> failAfter(Duration duration) {
        final CompletableFuture<T> promise = new CompletableFuture<>();

        scheduler.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout after " + duration);
            promise.completeExceptionally(ex);
        }, duration.toMillis(), TimeUnit.MILLISECONDS);

        return promise;
    }


    public static <T> T getValue(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> CompletableFuture<T> within(CompletableFuture<T> future, Duration duration) {
        final CompletableFuture<T> timeout = failAfter(duration);
        return future.applyToEither(timeout, Function.identity());
    }

    public static void main(String[] args) throws InterruptedException {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("f1");
            return "f1";
        });

        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            return "f2";
        });

        CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("f3");
            try {
                Thread.sleep(1200);
                System.out.println("f3　end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "f3";
        });

        CompletableFuture<String> f4 = CompletableFuture.supplyAsync(() -> {
            System.out.println("f4");
            try {
                Thread.sleep(1300);
                System.out.println("f4　end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "f4";
        });

        CompletableFuture<String> w1 = CompletableFutureUtil.within(f1, Duration.ofSeconds(1));
        CompletableFuture<String> w3 = CompletableFutureUtil.within(f3, Duration.ofSeconds(1));
        CompletableFuture<String> w4 = CompletableFutureUtil.within(f4, Duration.ofSeconds(14));

        f1.thenApply(r -> {

            CompletableFuture<String> w2 = CompletableFutureUtil.within(f2, Duration.ofSeconds(1));

            w2.thenApply(w -> {
                        System.out.print(w);
                        return w;
                    }
            );
            System.out.println(r);
            return null;
        });

        w1.handle((str, e) -> {
            if (e != null && e.getCause() instanceof TimeoutException) {
                System.out.println(e.getMessage());
//                f1.cancel(true);
                f1.complete("fff1");
            }
            return null;
        });


        Thread.sleep(10000);
    }

    public static void main1(String[] args) throws InterruptedException {

        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("before f1 thread count:" + ForkJoinPool.commonPool().getActiveThreadCount());
                System.out.println("f1");
                Thread.sleep(10000);
                System.out.println("f1 end.");
                System.out.println("after f1 thread count:" + ForkJoinPool.commonPool().getActiveThreadCount());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "c1";
        });


        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("before f2 thread count:" + ForkJoinPool.commonPool().getActiveThreadCount());
                System.out.println("f2");
                Thread.sleep(11000);
                System.out.println("f2 end.");
                System.out.println("after f2 thread count:" + ForkJoinPool.commonPool().getActiveThreadCount());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "c2";
        });

        CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("before f3 thread count:" + ForkJoinPool.commonPool().getActiveThreadCount());
                System.out.println("f3");
                Thread.sleep(12000);
                System.out.println("f3 end.");
                System.out.println("after f3 thread count:" + ForkJoinPool.commonPool().getActiveThreadCount());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "c3";
        });

        CompletableFuture<String> f4 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("before f4 thread count:" + ForkJoinPool.commonPool().getActiveThreadCount());
                System.out.println("f4");
                Thread.sleep(13000);
                System.out.println("f4 end.");
                System.out.println("after f4 thread count:" + ForkJoinPool.commonPool().getActiveThreadCount());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "c4";
        });

        Thread.sleep(1000000);
    }
}
