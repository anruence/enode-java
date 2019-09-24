package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class VertxRuntimeException extends RuntimeException {

    public VertxRuntimeException() {
        super();
    }

    public VertxRuntimeException(String msg) {
        super(msg);
    }

    public VertxRuntimeException(Throwable cause) {
        super(cause);
    }

    public VertxRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
