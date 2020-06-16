package org.enodeframework.eventing;

import java.util.List;

public class AggregateEventAppendResult {

    private EventAppendStatus eventAppendStatus;

    private List<String> duplicateCommandIds;

    public EventAppendStatus getEventAppendStatus() {
        return eventAppendStatus;
    }

    public void setEventAppendStatus(EventAppendStatus eventAppendStatus) {
        this.eventAppendStatus = eventAppendStatus;
    }

    public List<String> getDuplicateCommandIds() {
        return duplicateCommandIds;
    }

    public void setDuplicateCommandIds(List<String> duplicateCommandIds) {
        this.duplicateCommandIds = duplicateCommandIds;
    }
}
