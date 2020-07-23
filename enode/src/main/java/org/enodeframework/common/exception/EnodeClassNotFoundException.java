package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class EnodeClassNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 2514628822193223823L;

    public EnodeClassNotFoundException() {
        super();
    }

    public EnodeClassNotFoundException(String msg) {
        super(msg);
    }

    public EnodeClassNotFoundException(Throwable cause) {
        super(cause);
    }

    public EnodeClassNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
