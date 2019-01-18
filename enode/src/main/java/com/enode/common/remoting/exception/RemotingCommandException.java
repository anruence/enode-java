package com.enode.common.remoting.exception;

public class RemotingCommandException extends RemotingException {
    private static final long serialVersionUID = 7266556468345131264L;

    public RemotingCommandException(String message) {
        super(message);
    }

    public RemotingCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
