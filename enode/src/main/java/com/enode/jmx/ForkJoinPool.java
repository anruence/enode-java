package com.enode.jmx;

public class ForkJoinPool implements ForkJoinPoolMBean {
    @Override
    public int getQueuedSubmissionCount() {
        return java.util.concurrent.ForkJoinPool.commonPool().getQueuedSubmissionCount();
    }

    @Override
    public int getActiveThreadCount() {
        return java.util.concurrent.ForkJoinPool.commonPool().getActiveThreadCount();
    }

    @Override
    public int getCommonPoolParallelism() {
        return java.util.concurrent.ForkJoinPool.getCommonPoolParallelism();
    }

    @Override
    public int getPoolSize() {
        return java.util.concurrent.ForkJoinPool.commonPool().getPoolSize();
    }

    @Override
    public long getQueuedTaskCount() {
        return java.util.concurrent.ForkJoinPool.commonPool().getQueuedTaskCount();
    }

    @Override
    public int getRunningThreadCount() {
        return java.util.concurrent.ForkJoinPool.commonPool().getRunningThreadCount();
    }

    @Override
    public long getStealCount() {
        return java.util.concurrent.ForkJoinPool.commonPool().getStealCount();
    }
}
