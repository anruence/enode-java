package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class RegisterComponentException extends RuntimeException {
    private static final long serialVersionUID = 2976713867727370181L;

    public RegisterComponentException() {
        super();
    }

    public RegisterComponentException(String msg) {
        super(msg);
    }

    public RegisterComponentException(Throwable cause) {
        super(cause);
    }

    public RegisterComponentException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
