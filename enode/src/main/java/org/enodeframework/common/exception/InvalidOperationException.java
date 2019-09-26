package org.enodeframework.common.exception;

public class InvalidOperationException extends ENodeRuntimeException {
    public InvalidOperationException() {
        super();
    }

    public InvalidOperationException(String msg) {
        super(msg);
    }

    public InvalidOperationException(Throwable cause) {
        super(cause);
    }

    public InvalidOperationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
