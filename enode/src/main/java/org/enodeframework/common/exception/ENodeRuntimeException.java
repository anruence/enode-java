package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class ENodeRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -8951926710590746149L;

    public ENodeRuntimeException() {
        super();
    }

    public ENodeRuntimeException(Throwable throwable) {
        super(throwable);
    }

    public ENodeRuntimeException(String msg) {
        super(msg);
    }

    public ENodeRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
