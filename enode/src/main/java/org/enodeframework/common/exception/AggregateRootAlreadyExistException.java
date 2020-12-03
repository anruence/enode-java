package org.enodeframework.common.exception;

/**
 * @author anruence@gmail.com
 */
public class AggregateRootAlreadyExistException extends RuntimeException {
    private final static String EXCEPTION_MESSAGE = "aggregate root [type=%s,id=%s] already exist in command context, cannot be added again.";

    public AggregateRootAlreadyExistException(String id, Class<?> type) {
        super(String.format(EXCEPTION_MESSAGE, type.getName(), id));
    }
}
