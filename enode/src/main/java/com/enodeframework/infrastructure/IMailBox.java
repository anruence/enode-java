package com.enodeframework.infrastructure;


import java.util.Date;
import java.util.concurrent.CompletableFuture;

public interface IMailBox<TMessage extends IMailBoxMessage, TMessageProcessResult> {

    String getRoutingKey();

    Date getLastActiveTime();

    boolean isRunning();

    boolean isPauseRequested();

    boolean isPaused();

    long getConsumingSequence();

    long getConsumedSequence();

    long getMaxMessageSequence();

    long getTotalUnConsumedMessageCount();

    void enqueueMessage(TMessage message);

    void tryRun();

    void pause();

    void resume();

    void completeRun();

    void resetConsumingSequence(long consumingSequence);

    void clear();

    CompletableFuture completeMessage(TMessage message, TMessageProcessResult result);

    boolean isInactive(int timeoutSeconds);

}
