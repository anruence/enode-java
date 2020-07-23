package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class AggregateRootTypeNotMatchException extends RuntimeException {

    public AggregateRootTypeNotMatchException() {
        super();
    }

    public AggregateRootTypeNotMatchException(String msg) {
        super(msg);
    }

    public AggregateRootTypeNotMatchException(Throwable cause) {
        super(cause);
    }

    public AggregateRootTypeNotMatchException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
