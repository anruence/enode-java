package com.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 * checked exception covert to non-checked exception
 */
public class EnodeRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -8951926710590746149L;

    /**
     * checked exception
     */
    private Throwable exception;

    public EnodeRuntimeException(Throwable e) {
        super(e.getMessage());
        exception = e instanceof EnodeRuntimeException ? ((EnodeRuntimeException) e).getException() : e;
    }

    public EnodeRuntimeException(String msg) {
        super(msg);
    }

    public EnodeRuntimeException(String msg, Throwable e) {
        super(msg);
        exception = e instanceof EnodeRuntimeException ? ((EnodeRuntimeException) e).getException() : e;
    }

    public Throwable getException() {
        return exception;
    }
}
