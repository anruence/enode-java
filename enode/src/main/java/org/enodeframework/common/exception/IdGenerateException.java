package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class IdGenerateException extends RuntimeException {

    private static final long serialVersionUID = -8951926710590746149L;

    public IdGenerateException() {
        super();
    }

    public IdGenerateException(Throwable throwable) {
        super(throwable);
    }

    public IdGenerateException(String msg) {
        super(msg);
    }

    public IdGenerateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
