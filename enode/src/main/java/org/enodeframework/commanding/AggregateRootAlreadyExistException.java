package org.enodeframework.commanding;

/**
 * @author anruence@gmail.com
 */
public class AggregateRootAlreadyExistException extends RuntimeException {
    private final static String EXCEPTIONMESSAGE = "Aggregate root [type=%s,id=%s] already exist in command context, cannot be added again.";

    public AggregateRootAlreadyExistException(Object id, Class type) {
        super(String.format(EXCEPTIONMESSAGE, type.getName(), id));
    }
}
