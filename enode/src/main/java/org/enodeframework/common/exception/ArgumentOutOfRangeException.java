package org.enodeframework.common.exception;

public class ArgumentOutOfRangeException extends RuntimeException {

    private static final long serialVersionUID = 2875478983753773571L;

    public ArgumentOutOfRangeException(String msg) {
        super(msg);
    }

    public ArgumentOutOfRangeException(Throwable cause) {
        super(cause);
    }

    public ArgumentOutOfRangeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
