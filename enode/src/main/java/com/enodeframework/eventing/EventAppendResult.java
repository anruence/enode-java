package com.enodeframework.eventing;

public enum EventAppendResult {
    Success(1),
    Failed(2),
    DuplicateEvent(3),
    DuplicateCommand(4);
    private int status;

    EventAppendResult(int status) {
        this.status = status;
    }

    public int status() {
        return status;
    }
}
