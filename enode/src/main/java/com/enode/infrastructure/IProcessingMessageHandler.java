package com.enode.infrastructure;

public interface IProcessingMessageHandler<X extends IProcessingMessage<X, Y>, Y extends IMessage> {
    void handleAsync(X processingMessage);
}