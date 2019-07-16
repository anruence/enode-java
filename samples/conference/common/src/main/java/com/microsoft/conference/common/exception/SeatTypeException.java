package com.microsoft.conference.common.exception;

/**
 * @author anruence@gmail.com
 */
public class SeatTypeException extends RuntimeException {

    public SeatTypeException() {
        super();
    }

    public SeatTypeException(String msg) {
        super(msg);
    }

    public SeatTypeException(Throwable cause) {
        super(cause);
    }

    public SeatTypeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
