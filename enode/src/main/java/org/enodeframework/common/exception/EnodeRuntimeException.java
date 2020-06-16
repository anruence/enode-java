package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class EnodeRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -8951926710590746149L;

    public EnodeRuntimeException() {
        super();
    }

    public EnodeRuntimeException(Throwable throwable) {
        super(throwable);
    }

    public EnodeRuntimeException(String msg) {
        super(msg);
    }

    public EnodeRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
