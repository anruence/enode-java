package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class HandlerRegisterException extends RuntimeException {

    private static final long serialVersionUID = -3652102021062999423L;

    public HandlerRegisterException() {
        super();
    }

    public HandlerRegisterException(String msg) {
        super(msg);
    }

    public HandlerRegisterException(Throwable cause) {
        super(cause);
    }

    public HandlerRegisterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
