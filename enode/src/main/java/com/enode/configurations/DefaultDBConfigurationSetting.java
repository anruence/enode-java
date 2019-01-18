package com.enode.configurations;

public class DefaultDBConfigurationSetting {
    /**
     * 数据库连接字符串
     */
    private String ConnectionString;
    /**
     * 命令表的默认名称；默认为：Command
     */
    private String commandTableName;
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
     * Command表的CommandId的唯一索引的默认名称；默认为：IX_Command_CommandId
     */
    private String commandTableCommandIdUniqueIndexName;
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

    public DefaultDBConfigurationSetting() {
        commandTableName = "Command";
        eventTableName = "EventStream";
        eventTableCount = 1;
        eventTableBulkCopyBatchSize = 1000;
        eventTableBulkCopyTimeout = 60;
        publishedVersionTableName = "PublishedVersion";
        lockKeyTableName = "LockKey";
        commandTableCommandIdUniqueIndexName = "IX_Command_CommandId";
        eventTableVersionUniqueIndexName = "IX_EventStream_AggId_Version";
        eventTableCommandIdUniqueIndexName = "IX_EventStream_AggId_CommandId";
        publishedVersionUniqueIndexName = "IX_PublishedVersion_AggId_Version";
        lockKeyPrimaryKeyName = "PK_LockKey";
    }

    public String getConnectionString() {
        return ConnectionString;
    }

    public void setConnectionString(String connectionString) {
        ConnectionString = connectionString;
    }

    public String getCommandTableName() {
        return commandTableName;
    }

    public void setCommandTableName(String commandTableName) {
        this.commandTableName = commandTableName;
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

    public String getCommandTableCommandIdUniqueIndexName() {
        return commandTableCommandIdUniqueIndexName;
    }

    public void setCommandTableCommandIdUniqueIndexName(String commandTableCommandIdUniqueIndexName) {
        this.commandTableCommandIdUniqueIndexName = commandTableCommandIdUniqueIndexName;
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
}
