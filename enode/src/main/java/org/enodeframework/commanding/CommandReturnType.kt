package org.enodeframework.commanding

/**
 * A enum defines the command result return type.
 */
enum class CommandReturnType(var value: Short) {
    /**
     * Return the command result when the command execution has the following cases:
     * 1. the command execution meets some error or exception;
     * 2. the command execution makes nothing changes of domain;
     * 3. the command execution success, and the domain event is sent to the message queue successfully.
     */
    CommandExecuted(1.toShort()),

    /**
     * Return the command result when the command execution has the following cases:
     * 1. the command execution meets some error or exception;
     * 2. the command execution makes nothing changes of domain;
     * 3. the command execution success, and the domain event is handled.
     */
    EventHandled(2.toShort());

}