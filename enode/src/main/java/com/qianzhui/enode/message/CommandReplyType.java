package com.qianzhui.enode.message;

public enum CommandReplyType {
    // send reply type
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
