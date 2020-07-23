package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class MailBoxProcessException extends RuntimeException {

    public MailBoxProcessException() {
        super();
    }

    public MailBoxProcessException(String msg) {
        super(msg);
    }

    public MailBoxProcessException(Throwable cause) {
        super(cause);
    }

    public MailBoxProcessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
