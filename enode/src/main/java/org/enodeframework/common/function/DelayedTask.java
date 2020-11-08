package org.enodeframework.common.function;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author anruence@gmail.com
 */
public class DelayedTask {
    private static final ScheduledExecutorService EXECUTOR = new ScheduledThreadPoolExecutor(
            1, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("DelayedThread-%d").build());

    public static void startDelayedTask(Duration duration, Action action) {
        DelayedTask.EXECUTOR.schedule(action::apply, duration.toMillis(), TimeUnit.MILLISECONDS);
    }
}
