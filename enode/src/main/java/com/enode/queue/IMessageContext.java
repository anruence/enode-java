package com.enode.queue;

public interface IMessageContext {

    void onMessageHandled(QueueMessage message);
}
