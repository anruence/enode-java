package com.enodeframework.infrastructure;

public interface IMailBoxMessage<TMessage extends IMailBoxMessage, TMessageProcessResult> {

    IMailBox<TMessage, TMessageProcessResult> getMailBox();

    void setMailBox(IMailBox<TMessage, TMessageProcessResult> mailBox);

    long getSequence();

    void setSequence(long sequence);
}
