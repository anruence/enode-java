package com.enode.infrastructure;

import java.util.Map;

public interface IPublishableException extends IMessage {
    /**
     * Serialize the current exception info to the given dictionary.
     *
     * @param serializableInfo
     */
    void serializeTo(Map<String, String> serializableInfo);

    /**
     * Restore the current exception from the given dictionary.
     *
     * @param serializableInfo
     */
    void restoreFrom(Map<String, String> serializableInfo);
}
