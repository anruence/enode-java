package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class MethodInvokeException extends RuntimeException {

    private static final long serialVersionUID = -3652102021062999423L;

    public MethodInvokeException() {
        super();
    }

    public MethodInvokeException(String msg) {
        super(msg);
    }

    public MethodInvokeException(Throwable cause) {
        super(cause);
    }

    public MethodInvokeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
