package org.enodeframework.domain;

import org.enodeframework.messaging.IMessage;

import java.util.Map;

public interface IDomainException extends IMessage {
    /**
     * Serialize the current exception info to the given dictionary.
     */
    void serializeTo(Map<String, Object> serializableInfo);

    /**
     * Restore the current exception from the given dictionary.
     */
    void restoreFrom(Map<String, Object> serializableInfo);
}
