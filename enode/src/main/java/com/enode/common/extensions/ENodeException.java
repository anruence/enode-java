package com.enode.common.extensions;

/**
 * ENode framework Exception
 */
public class ENodeException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ENodeException(String errMessage) {
        super(errMessage);
    }

    public ENodeException(String errMessage, Throwable e) {
        super(errMessage, e);
    }
}