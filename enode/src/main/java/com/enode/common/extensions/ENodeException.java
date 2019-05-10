package com.enode.common.extensions;

/**
 * ENode framework Exception
 */
public class ENodeException extends BaseException {

    public ENodeException(String errMessage) {
        super(errMessage);
    }

    public ENodeException(String errMessage, Throwable e) {
        super(errMessage, e);
    }
}