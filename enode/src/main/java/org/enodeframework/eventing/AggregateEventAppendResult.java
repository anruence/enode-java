package org.enodeframework.eventing;

import com.google.common.collect.Lists;

import java.util.List;

public class AggregateEventAppendResult {

    private EventAppendStatus eventAppendStatus;

    private List<String> duplicateCommandIds = Lists.newArrayList();

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
