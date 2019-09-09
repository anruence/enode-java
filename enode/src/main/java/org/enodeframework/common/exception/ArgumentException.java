package org.enodeframework.common.exception;

public class ArgumentException extends RuntimeException {
    public ArgumentException() {
        super();
    }

    public ArgumentException(String msg) {
        super(msg);
    }

    public ArgumentException(Throwable cause) {
        super(cause);
    }

    public ArgumentException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
