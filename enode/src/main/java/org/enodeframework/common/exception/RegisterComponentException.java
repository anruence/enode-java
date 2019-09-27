package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class RegisterComponentException extends ENodeRuntimeException {

    private static final long serialVersionUID = -3652102021062999423L;

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
