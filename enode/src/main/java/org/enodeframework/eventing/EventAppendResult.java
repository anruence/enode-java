package org.enodeframework.eventing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class EventAppendResult {

    private final Object lockObj = new Object();

    private List<String> successAggregateRootIdList = Lists.newArrayList();

    private List<String> duplicateEventAggregateRootIdList = Lists.newArrayList();

    private Map<String, List<String>> duplicateCommandAggregateRootIdList = Maps.newHashMap();

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

    public Map<String, List<String>> getDuplicateCommandAggregateRootIdList() {
        return duplicateCommandAggregateRootIdList;
    }

    public void setDuplicateCommandAggregateRootIdList(Map<String, List<String>> duplicateCommandAggregateRootIdList) {
        this.duplicateCommandAggregateRootIdList = duplicateCommandAggregateRootIdList;
    }

    public void addSuccessAggregateRootId(String aggregateRootId) {
        synchronized (lockObj) {
            if (!successAggregateRootIdList.contains(aggregateRootId)) {
                successAggregateRootIdList.add(aggregateRootId);
            }
        }
    }

    public void addDuplicateEventAggregateRootId(String aggregateRootId) {
        synchronized (lockObj) {
            if (!duplicateEventAggregateRootIdList.contains(aggregateRootId)) {
                duplicateEventAggregateRootIdList.add(aggregateRootId);
            }
        }
    }

    public void addDuplicateCommandIds(String aggregateRootId, List<String> aggregateDuplicateCommandIdList) {
        synchronized (lockObj) {
            if (!duplicateCommandAggregateRootIdList.containsKey(aggregateRootId)) {
                duplicateCommandAggregateRootIdList.put(aggregateRootId, aggregateDuplicateCommandIdList);
            }
        }
    }
}
