package com.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 * checked exception covert to non-checked exception
 */
public class ENodeRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -8951926710590746149L;
    /**
     * checked exception
     */
    private Throwable exception;

    public ENodeRuntimeException(Throwable e) {
        super(e.getMessage());
        exception = e instanceof ENodeRuntimeException ? ((ENodeRuntimeException) e).getException() : e;
    }

    public ENodeRuntimeException(String msg) {
        super(msg);
    }

    public ENodeRuntimeException(String msg, Throwable e) {
        super(msg);
        exception = e instanceof ENodeRuntimeException ? ((ENodeRuntimeException) e).getException() : e;
    }

    public Throwable getException() {
        return exception;
    }
}
