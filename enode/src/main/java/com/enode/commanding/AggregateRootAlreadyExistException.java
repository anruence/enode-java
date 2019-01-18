package com.enode.commanding;

public class AggregateRootAlreadyExistException extends RuntimeException {
    private final static String ExceptionMessage = "Aggregate root [type=%s,id=%s] already exist in command context, cannot be added again.";

    public AggregateRootAlreadyExistException(Object id, Class type) {
        super(String.format(ExceptionMessage, type.getName(), id));
    }

}
