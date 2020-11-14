package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class AggregateRootNotFoundException extends RuntimeException {

    public AggregateRootNotFoundException() {
        super();
    }

    public AggregateRootNotFoundException(String msg) {
        super(msg);
    }

    public AggregateRootNotFoundException(Throwable cause) {
        super(cause);
    }

    public AggregateRootNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
