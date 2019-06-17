package com.enodeframework.common.io;

/**
 * @author anruence@gmail.com
 */
public class IORuntimeException extends RuntimeException {
    private static final long serialVersionUID = 2976713867727370181L;

    public IORuntimeException() {
        super();
    }

    public IORuntimeException(String msg) {
        super(msg);
    }

    public IORuntimeException(Throwable cause) {
        super(cause);
    }

    public IORuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
