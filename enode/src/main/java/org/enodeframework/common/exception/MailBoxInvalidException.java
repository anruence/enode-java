package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class MailBoxInvalidException extends RuntimeException {

    public MailBoxInvalidException() {
        super();
    }

    public MailBoxInvalidException(String msg) {
        super(msg);
    }

    public MailBoxInvalidException(Throwable cause) {
        super(cause);
    }

    public MailBoxInvalidException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
