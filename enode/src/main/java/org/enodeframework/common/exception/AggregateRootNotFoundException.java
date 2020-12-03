package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class AggregateRootNotFoundException extends RuntimeException {
    private static final String EXCEPTION_MESSAGE = "aggregate root [type=%s,id=%s] not found.";

    public AggregateRootNotFoundException() {
        super();
    }

    public AggregateRootNotFoundException(String msg) {
        super(msg);
    }

    public AggregateRootNotFoundException(String id, Class<?> type) {
        super(String.format(EXCEPTION_MESSAGE, type.getName(), id));
    }

    public AggregateRootNotFoundException(Throwable cause) {
        super(cause);
    }

    public AggregateRootNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
