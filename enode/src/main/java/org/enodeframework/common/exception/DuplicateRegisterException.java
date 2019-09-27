package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class DuplicateRegisterException extends ENodeRuntimeException {

    private static final long serialVersionUID = -3707822511264547141L;

    public DuplicateRegisterException() {
        super();
    }

    public DuplicateRegisterException(String msg) {
        super(msg);
    }

    public DuplicateRegisterException(Throwable cause) {
        super(cause);
    }

    public DuplicateRegisterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
