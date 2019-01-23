package com.enode.queue;

public class CompletableConsumeConcurrentlyContext<T extends Object> {

    private final T messageQueue;
    /**
     * Message consume retry strategy<br>
     * -1，no retry,put into DLQ directly<br>
     * 0，broker control retry frequency<br>
     * >0，client control retry frequency
     */
    private int delayLevelWhenNextConsume = 0;
    private int ackIndex = Integer.MAX_VALUE;

    public CompletableConsumeConcurrentlyContext(T messageQueue) {
        this.messageQueue = messageQueue;
    }

    public int getDelayLevelWhenNextConsume() {
        return delayLevelWhenNextConsume;
    }

    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
        this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
    }

    public void onMessageHandled() {
    }

    public void reConsumeLater() {
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
