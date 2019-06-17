package com.enodeframework.infrastructure;

/**
 * checked exception covert to non-checked exception
 */

/**
 * @author anruence@gmail.com
 */
public class WrappedRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -8951926710590746149L;

    /**
     * checked exception
     */
    private Throwable exception;

    public WrappedRuntimeException(Throwable e) {
        super(e.getMessage());
        exception = e instanceof WrappedRuntimeException ? ((WrappedRuntimeException) e).getException() : e;
    }

    public WrappedRuntimeException(String msg) {
        super(msg);
    }

    public WrappedRuntimeException(String msg, Throwable e) {
        super(msg);
        exception = e instanceof WrappedRuntimeException ? ((WrappedRuntimeException) e).getException() : e;
    }

    public Throwable getException() {
        return exception;
    }
}
