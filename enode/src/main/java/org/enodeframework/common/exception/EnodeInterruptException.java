package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class EnodeInterruptException extends RuntimeException {

    private static final long serialVersionUID = 5391482343851766256L;

    public EnodeInterruptException(Throwable throwable) {
        super(throwable);
    }

}
