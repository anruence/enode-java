package org.enodeframework.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步线程池
 */
public class EnodeThreadPoolAutoConfig {

    @Value("${async.executor.thread.core.size:10}")
    private final int corePoolSize = 10;

    @Value("${async.executor.thread.max.size:100}")
    private final int maxPoolSize = 100;

    @Value("${async.executor.thread.queue.capacity:1024}")
    private final int queueCapacity = 1024;

    @Value("${async.executor.thread.keepalive:0}")
    private final int keepAliveSeconds = 0;

    private final CallerRunsPolicy callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy();
    //线程池对拒绝任务(无线程可用)的处理策略

    private static final Logger LOGGER = LoggerFactory.getLogger(EnodeThreadPoolAutoConfig.class);

    private static final Thread.UncaughtExceptionHandler MAIL_BOX_MESSAGE_EXECUTOR_EXCEPTION_HANDLER =
            (t, e) -> LOGGER.error("Uncaught Exception from asyncCommandExecutor. threadName = {}", t.getName(), e);


    @Bean(name = "mailBoxExecutor")
    public ThreadPoolTaskExecutor mailBoxExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("MailBoxMessageExecutor-");
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setRejectedExecutionHandler(callerRunsPolicy);
        executor.setThreadFactory(new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "MailBoxMessageExecutor-" + threadNumber.getAndIncrement());
                thread.setUncaughtExceptionHandler(MAIL_BOX_MESSAGE_EXECUTOR_EXCEPTION_HANDLER);
                return thread;
            }
        });
        executor.setRejectedExecutionHandler(callerRunsPolicy);
        executor.initialize();
        return executor;
    }
}
