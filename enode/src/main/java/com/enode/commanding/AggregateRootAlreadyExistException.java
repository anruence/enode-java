package com.enode.commanding;

public class AggregateRootAlreadyExistException extends RuntimeException {

    private final static String EXCEPTION_MESSAGE = "Aggregate root [type=%s,id=%s] already exist in command context, cannot be added again.";

    public AggregateRootAlreadyExistException(Object id, Class type) {
        super(String.format(EXCEPTION_MESSAGE, type.getName(), id));
    }

}
