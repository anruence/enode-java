package com.microsoft.conference.common.exception;

public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException() {
    }

    public InvalidOperationException(String s) {
        super(s);
    }
}
