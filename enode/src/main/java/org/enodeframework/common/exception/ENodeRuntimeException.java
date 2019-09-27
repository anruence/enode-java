package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 * checked exception covert to non-checked exception
 */
public class ENodeRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -8951926710590746149L;

    private Throwable exception;

    public ENodeRuntimeException() {
        super();
    }

    public ENodeRuntimeException(Throwable throwable) {
        super(throwable.getMessage());
        exception = throwable instanceof ENodeRuntimeException ? ((ENodeRuntimeException) throwable).getException() : throwable;
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
