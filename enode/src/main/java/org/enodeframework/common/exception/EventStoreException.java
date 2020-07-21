package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class EventStoreException extends RuntimeException {

    public EventStoreException() {
        super();
    }

    public EventStoreException(String msg) {
        super(msg);
    }

    public EventStoreException(Throwable cause) {
        super(cause);
    }

    public EventStoreException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
