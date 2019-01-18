package com.enode.rocketmq.consumer.listener;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.common.message.MessageQueue;

import java.util.concurrent.CompletableFuture;

public class CompletableConsumeConcurrentlyContext {
    private final MessageQueue messageQueue;
    private final CompletableFuture<ConsumeConcurrentlyStatus> statusFuture;
    /**
     * Message consume retry strategy<br>
     * -1，no retry,put into DLQ directly<br>
     * 0，broker control retry frequency<br>
     * >0，client control retry frequency
     */
    private int delayLevelWhenNextConsume = 0;
    private int ackIndex = Integer.MAX_VALUE;

    public CompletableConsumeConcurrentlyContext(MessageQueue messageQueue, CompletableFuture<ConsumeConcurrentlyStatus> statusFuture) {
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

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }


    public int getAckIndex() {
        return ackIndex;
    }


    public void setAckIndex(int ackIndex) {
        this.ackIndex = ackIndex;
    }
}
