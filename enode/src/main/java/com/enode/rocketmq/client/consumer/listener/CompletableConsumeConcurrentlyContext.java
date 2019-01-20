package com.enode.rocketmq.client.consumer.listener;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;

import java.util.concurrent.CompletableFuture;

public class CompletableConsumeConcurrentlyContext<T extends Object> {
    private final T messageQueue;
    private final CompletableFuture statusFuture;
    /**
     * Message consume retry strategy<br>
     * -1，no retry,put into DLQ directly<br>
     * 0，broker control retry frequency<br>
     * >0，client control retry frequency
     */
    private int delayLevelWhenNextConsume = 0;
    private int ackIndex = Integer.MAX_VALUE;

    public CompletableConsumeConcurrentlyContext(T messageQueue, CompletableFuture<ConsumeConcurrentlyStatus> statusFuture) {
        this.messageQueue = messageQueue;
        this.statusFuture = statusFuture;
    }

    public int getDelayLevelWhenNextConsume() {
        return delayLevelWhenNextConsume;
    }

    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
        this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
    }

    public void onMessageHandled() {
        statusFuture.complete(ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
    }

    public void reConsumeLater() {
        statusFuture.complete(ConsumeConcurrentlyStatus.RECONSUME_LATER);
    }

    public T getMessageQueue() {
        return messageQueue;
    }


    public int getAckIndex() {
        return ackIndex;
    }


    public void setAckIndex(int ackIndex) {
        this.ackIndex = ackIndex;
    }
}
