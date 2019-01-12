package com.qianzhui.enode.rocketmq;

public enum CommandReplyType {
    CommandExecuted((short) 1),
    DomainEventHandled((short) 2);

    short value;

    CommandReplyType(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}
