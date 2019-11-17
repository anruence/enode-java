package org.enodeframework.messaging;

import java.util.Date;
import java.util.Map;

public interface IMessage {
    /**
     * Represents the unique identifier of the message.
     */
    String getId();

    void setId(String id);

    /**
     * Represents the timestamp of the message.
     */
    Date getTimestamp();

    void setTimestamp(Date timestamp);

    /**
     * Represents the extension key/values data of the message.
     */
    Map<String, String> getItems();


    void setItems(Map<String, String> items);

    /**
     * Merge the givens key/values into the current Items.
     */
    void mergeItems(Map<String, String> items);
}
