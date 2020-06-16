package org.enodeframework.mysql;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * @author anruence@gmail.com
 */
public class StreamRecord {

    @JsonProperty("id")
    public String id;

    @JsonProperty("aggregate_root_type_name")
    public String aggregateRootTypeName;

    @JsonProperty("aggregate_root_id")
    public String aggregateRootId;

    public int version;

    @JsonProperty("command_id")
    public String commandId;

    @JsonProperty("gmt_create")
    public Date gmtCreated;

    public String events;

    public StreamRecord() {

    }
}
