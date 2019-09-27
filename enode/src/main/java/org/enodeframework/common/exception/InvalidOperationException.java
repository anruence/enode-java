package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class InvalidOperationException extends ENodeRuntimeException {

    private static final long serialVersionUID = 6201111149059858558L;

    public InvalidOperationException() {
        super();
    }

    public InvalidOperationException(String msg) {
        super(msg);
    }

    public InvalidOperationException(Throwable cause) {
        super(cause);
    }

    public InvalidOperationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
