package com.enode.common.extensions;

/**
 * Base Exception is the parent of all exceptions
 */
public abstract class BaseException extends RuntimeException {

    public BaseException(String errMessage) {
        super(errMessage);
    }

    public BaseException(String errMessage, Throwable e) {
        super(errMessage, e);
    }

}
