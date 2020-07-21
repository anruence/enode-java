package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class DomainEventInvalidException extends RuntimeException {

    public DomainEventInvalidException() {
        super();
    }

    public DomainEventInvalidException(String msg) {
        super(msg);
    }

    public DomainEventInvalidException(Throwable cause) {
        super(cause);
    }

    public DomainEventInvalidException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
