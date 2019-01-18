package com.enode.common.remoting.exception;

public class RemotingException extends Exception {

    private static final long serialVersionUID = 1895474456020330046L;

    public RemotingException(String message) {
        super(message);
    }

    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}
