package com.enode.queue;

public interface IMessageHandler {

    void handle(QueueMessage queueMessage, IMessageContext context);
}
