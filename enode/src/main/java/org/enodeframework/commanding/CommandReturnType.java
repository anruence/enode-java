package org.enodeframework.commanding;

/**
 * A enum defines the command result return type.
 */
public enum CommandReturnType {
    /**
     * Return the command result when the command execution has the following cases:
     * 1. the command execution meets some error or exception;
     * 2. the command execution makes nothing changes of domain;
     * 3. the command execution success, and the domain event is sent to the message queue successfully.
     */
    CommandExecuted((short) 1),
    /**
     * Return the command result when the command execution has the following cases:
     * 1. the command execution meets some error or exception;
     * 2. the command execution makes nothing changes of domain;
     * 3. the command execution success, and the domain event is handled.
     */
    EventHandled((short) 2);
    short value;

    CommandReturnType(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}
