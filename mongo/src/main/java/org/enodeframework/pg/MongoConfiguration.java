package org.enodeframework.pg;

/**
 * @author anruence@gmail.com
 */
public class MongoConfiguration {
    /**
     * 聚合唯一键冲突时的错误码
     */
    private int duplicateCode;
    /**
     * 事件表存储的默认名称；默认为：event_stream
     */
    private String eventCollectionName;
    /**
     * MongoDB 为应用创建的database名称
     */
    private String databaseName;

    /**
     * 聚合根已发布事件表的默认名称；默认为：published_version
     */
    private String publishedVersionCollectionName;

    /**
     * 事件表的聚合根版本唯一索引的默认名称；默认为：IX_EventStream_AggId_Version
     */
    private String eventTableVersionUniqueIndexName;
    /**
     * 事件表的聚合根已处理命令唯一索引的默认名称；默认为：IX_EventStream_AggId_CommandId
     */
    private String eventTableCommandIdUniqueIndexName;
    /**
     * 聚合根已发布事件表的聚合根已发布版本唯一索引的默认名称；默认为：IX_PublishedVersion_AggId_Version
     */
    private String publishedVersionUniqueIndexName;

    public MongoConfiguration() {
        databaseName = "enode";
        eventCollectionName = "event_stream";
        publishedVersionCollectionName = "published_version";
        eventTableVersionUniqueIndexName = "aggregateRootId_1_version_1";
        eventTableCommandIdUniqueIndexName = "aggregateRootId_1_commandId_1";
        publishedVersionUniqueIndexName = "processorName_1_aggregateRootId_1_version_1";
        duplicateCode = 11000;
    }

    public String getEventCollectionName() {
        return eventCollectionName;
    }

    public void setEventCollectionName(String eventCollectionName) {
        this.eventCollectionName = eventCollectionName;
    }

    public String getPublishedVersionCollectionName() {
        return publishedVersionCollectionName;
    }

    public void setPublishedVersionCollectionName(String publishedVersionCollectionName) {
        this.publishedVersionCollectionName = publishedVersionCollectionName;
    }

    public String getEventTableVersionUniqueIndexName() {
        return eventTableVersionUniqueIndexName;
    }

    public void setEventTableVersionUniqueIndexName(String eventTableVersionUniqueIndexName) {
        this.eventTableVersionUniqueIndexName = eventTableVersionUniqueIndexName;
    }

    public String getEventTableCommandIdUniqueIndexName() {
        return eventTableCommandIdUniqueIndexName;
    }

    public void setEventTableCommandIdUniqueIndexName(String eventTableCommandIdUniqueIndexName) {
        this.eventTableCommandIdUniqueIndexName = eventTableCommandIdUniqueIndexName;
    }

    public String getPublishedVersionUniqueIndexName() {
        return publishedVersionUniqueIndexName;
    }

    public void setPublishedVersionUniqueIndexName(String publishedVersionUniqueIndexName) {
        this.publishedVersionUniqueIndexName = publishedVersionUniqueIndexName;
    }

    public int getDuplicateCode() {
        return duplicateCode;
    }

    public void setDuplicateCode(int duplicateCode) {
        this.duplicateCode = duplicateCode;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
