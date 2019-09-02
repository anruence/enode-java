package com.enodeframework.eventing;

import com.google.common.collect.Lists;

import java.util.List;

public class EventAppendResult {
    public static EventAppendResult Success = new EventAppendResult();
    public static EventAppendResult Failed = new EventAppendResult();
    public static EventAppendResult DuplicateEvent = new EventAppendResult();
    public static EventAppendResult DuplicateCommand = new EventAppendResult();

    public List<String> successAggregateRootIdList = Lists.newArrayList();
    public List<String> duplicateEventAggregateRootIdList = Lists.newArrayList();
    public List<String> duplicateCommandIdList = Lists.newArrayList();

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
