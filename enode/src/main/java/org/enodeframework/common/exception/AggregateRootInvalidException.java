package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class AggregateRootInvalidException extends RuntimeException {

    public AggregateRootInvalidException() {
        super();
    }

    public AggregateRootInvalidException(String msg) {
        super(msg);
    }

    public AggregateRootInvalidException(Throwable cause) {
        super(cause);
    }

    public AggregateRootInvalidException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
