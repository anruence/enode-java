package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class RemotingException extends ENodeRuntimeException {

    private static final long serialVersionUID = 2514628822193223823L;

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
