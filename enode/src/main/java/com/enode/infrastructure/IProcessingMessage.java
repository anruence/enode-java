package com.enode.infrastructure;

public interface IProcessingMessage<X extends IProcessingMessage<X, Y>, Y extends IMessage> {
    Y getMessage();

    void setMailbox(ProcessingMessageMailbox<X, Y> mailbox);

    void complete();
}
