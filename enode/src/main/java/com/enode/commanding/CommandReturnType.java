package com.enode.commanding;

public enum CommandReturnType {

    CommandExecuted((short) 1),
    EventHandled((short) 2);

    short value;

    CommandReturnType(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}
