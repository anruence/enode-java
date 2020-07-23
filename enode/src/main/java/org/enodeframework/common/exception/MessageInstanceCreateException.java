package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class MessageInstanceCreateException extends RuntimeException {

    public MessageInstanceCreateException() {
        super();
    }

    public MessageInstanceCreateException(String msg) {
        super(msg);
    }

    public MessageInstanceCreateException(Throwable cause) {
        super(cause);
    }

    public MessageInstanceCreateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
