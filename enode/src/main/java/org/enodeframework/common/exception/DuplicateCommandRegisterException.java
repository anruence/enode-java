package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class DuplicateCommandRegisterException extends RuntimeException {

    public DuplicateCommandRegisterException() {
        super();
    }

    public DuplicateCommandRegisterException(String msg) {
        super(msg);
    }

    public DuplicateCommandRegisterException(Throwable cause) {
        super(cause);
    }

    public DuplicateCommandRegisterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
