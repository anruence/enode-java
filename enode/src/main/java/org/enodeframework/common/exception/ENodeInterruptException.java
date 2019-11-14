package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class ENodeInterruptException extends RuntimeException {

    private static final long serialVersionUID = 5391482343851766256L;

    public ENodeInterruptException(Throwable throwable) {
        super(throwable);
    }

}
