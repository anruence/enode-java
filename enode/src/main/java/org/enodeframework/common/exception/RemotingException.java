package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class RemotingException extends RuntimeException {
    private static final long serialVersionUID = 2976713867727370181L;

    public RemotingException() {
        super();
    }

    public RemotingException(String msg) {
        super(msg);
    }

    public RemotingException(Throwable cause) {
        super(cause);
    }

    public RemotingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
