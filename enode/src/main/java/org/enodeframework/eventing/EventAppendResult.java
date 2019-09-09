package org.enodeframework.eventing;

import com.google.common.collect.Lists;

import java.util.List;

public class EventAppendResult {

    private List<String> successAggregateRootIdList = Lists.newArrayList();
    private List<String> duplicateEventAggregateRootIdList = Lists.newArrayList();
    private List<String> duplicateCommandIdList = Lists.newArrayList();

    public List<String> getSuccessAggregateRootIdList() {
        return successAggregateRootIdList;
    }

    public void setSuccessAggregateRootIdList(List<String> successAggregateRootIdList) {
        this.successAggregateRootIdList = successAggregateRootIdList;
    }

    public List<String> getDuplicateEventAggregateRootIdList() {
        return duplicateEventAggregateRootIdList;
    }

    public void setDuplicateEventAggregateRootIdList(List<String> duplicateEventAggregateRootIdList) {
        this.duplicateEventAggregateRootIdList = duplicateEventAggregateRootIdList;
    }

    public List<String> getDuplicateCommandIdList() {
        return duplicateCommandIdList;
    }

    public void setDuplicateCommandIdList(List<String> duplicateCommandIdList) {
        this.duplicateCommandIdList = duplicateCommandIdList;
    }

}
