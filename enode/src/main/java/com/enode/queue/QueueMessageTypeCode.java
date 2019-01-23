package com.enode.queue;

public enum QueueMessageTypeCode {
    //为保持与C#版本一致
    Other(0),
    CommandMessage(1),
    DomainEventStreamMessage(2),
    ExceptionMessage(3),
    ApplicationMessage(4);

    int value;

    QueueMessageTypeCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
