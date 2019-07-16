package com.microsoft.conference.common.exception;

/**
 * @author anruence@gmail.com
 */
public class RegisterSlugException extends RuntimeException {

    public RegisterSlugException() {
        super();
    }

    public RegisterSlugException(String msg) {
        super(msg);
    }

    public RegisterSlugException(Throwable cause) {
        super(cause);
    }

    public RegisterSlugException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
