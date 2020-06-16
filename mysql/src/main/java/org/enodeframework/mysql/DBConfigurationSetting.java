package org.enodeframework.mysql;

/**
 * @author anruence@gmail.com
 */
public class DBConfigurationSetting {
    /**
     * mysql唯一键冲突时的错误码
     */
    private int duplicateCode;
    /**
     * 事件表的默认名称；默认为：EventStream
     */
    private String eventTableName;
    /**
     * 事件表的默认个数，用于支持最简易的单库分表；默认为：1，即不分表
     */
    private int eventTableCount;
    /**
     * 事件表批量持久化单批最大事件数；默认为：1000
     */
    private int eventTableBulkCopyBatchSize;
    /**
     * 事件表批量持久化单批超时时间；单位为秒，默认为：60s
     */
    private int eventTableBulkCopyTimeout;
    /**
     * 聚合根已发布事件表的默认名称；默认为：PublishedVersion
     */
    private String publishedVersionTableName;
    /**
     * LockKey表的默认名称；默认为：LockKey
     */
    private String lockKeyTableName;
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
    /**
     * LockKey表的默认主键的名称；默认为：PK_LockKey
     */
    private String lockKeyPrimaryKeyName;

    public DBConfigurationSetting() {
        eventTableName = "event_stream";
        eventTableCount = 1;
        eventTableBulkCopyBatchSize = 1000;
        eventTableBulkCopyTimeout = 60;
        publishedVersionTableName = "published_version";
        lockKeyTableName = "LockKey";
        eventTableVersionUniqueIndexName = "uk_aggregate_root_id_version";
        eventTableCommandIdUniqueIndexName = "uk_aggregate_root_id_command_id";
        publishedVersionUniqueIndexName = "uk_processor_name_aggregate_root_id_version";
        lockKeyPrimaryKeyName = "PK_LockKey";
        duplicateCode = 1062;
    }

    public String getEventTableName() {
        return eventTableName;
    }

    public void setEventTableName(String eventTableName) {
        this.eventTableName = eventTableName;
    }

    public int getEventTableCount() {
        return eventTableCount;
    }

    public void setEventTableCount(int eventTableCount) {
        this.eventTableCount = eventTableCount;
    }

    public int getEventTableBulkCopyBatchSize() {
        return eventTableBulkCopyBatchSize;
    }

    public void setEventTableBulkCopyBatchSize(int eventTableBulkCopyBatchSize) {
        this.eventTableBulkCopyBatchSize = eventTableBulkCopyBatchSize;
    }

    public int getEventTableBulkCopyTimeout() {
        return eventTableBulkCopyTimeout;
    }

    public void setEventTableBulkCopyTimeout(int eventTableBulkCopyTimeout) {
        this.eventTableBulkCopyTimeout = eventTableBulkCopyTimeout;
    }

    public String getPublishedVersionTableName() {
        return publishedVersionTableName;
    }

    public void setPublishedVersionTableName(String publishedVersionTableName) {
        this.publishedVersionTableName = publishedVersionTableName;
    }

    public String getLockKeyTableName() {
        return lockKeyTableName;
    }

    public void setLockKeyTableName(String lockKeyTableName) {
        this.lockKeyTableName = lockKeyTableName;
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

    public String getLockKeyPrimaryKeyName() {
        return lockKeyPrimaryKeyName;
    }

    public void setLockKeyPrimaryKeyName(String lockKeyPrimaryKeyName) {
        this.lockKeyPrimaryKeyName = lockKeyPrimaryKeyName;
    }

    public int getDuplicateCode() {
        return duplicateCode;
    }

    public void setDuplicateCode(int duplicateCode) {
        this.duplicateCode = duplicateCode;
    }
}
