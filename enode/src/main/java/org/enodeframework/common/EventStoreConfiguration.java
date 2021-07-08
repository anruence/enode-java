package org.enodeframework.common;

/**
 * 可以将EventStore的元数据全部配置化
 *
 * @author anruence@gmail.com
 */
public class EventStoreConfiguration {
    /**
     * 数据库类型
     */
    private String dbType;
    /**
     * MongoDB 为应用创建的database名称
     */
    private String dbName;
    /**
     * 事件表的默认名称；默认为：event_stream
     */
    private String eventTableName;
    /**
     * 聚合根已发布事件表的默认名称；默认为：published_version
     */
    private String publishedTableName;
    /**
     * 事件表的聚合根版本唯一索引的默认名称；默认为：uk_aggregate_root_id_version
     */
    private String eventVersionUkName;
    /**
     * 事件表的聚合根已处理命令唯一索引的默认名称；默认为：uk_aggregate_root_id_command_id
     */
    private String eventCommandIdUkName;
    /**
     * 聚合根已发布事件表的聚合根已发布版本唯一索引的默认名称；默认为：uk_processor_name_aggregate_root_id_version
     */
    private String publishedUkName;

    public static EventStoreConfiguration jdbc() {
        EventStoreConfiguration configuration = new EventStoreConfiguration();
        configuration.setDbName("enode");
        configuration.setDbType(DbType.MySQL.name());
        configuration.setEventTableName("event_stream");
        configuration.setPublishedTableName("published_version");
        configuration.setEventVersionUkName("uk_aggregate_root_id_version");
        configuration.setEventCommandIdUkName("uk_aggregate_root_id_command_id");
        configuration.setPublishedUkName("uk_processor_name_aggregate_root_id");
        return configuration;
    }

    public static EventStoreConfiguration mysql() {
        EventStoreConfiguration configuration = new EventStoreConfiguration();
        configuration.setDbName("enode");
        configuration.setDbType(DbType.MySQL.name());
        configuration.setEventTableName("event_stream");
        configuration.setPublishedTableName("published_version");
        configuration.setEventVersionUkName("uk_aggregate_root_id_version");
        configuration.setEventCommandIdUkName("uk_aggregate_root_id_command_id");
        configuration.setPublishedUkName("uk_processor_name_aggregate_root_id");
        return configuration;
    }

    public static EventStoreConfiguration mongo() {
        EventStoreConfiguration configuration = new EventStoreConfiguration();
        configuration.setDbName("enode");
        configuration.setDbType(DbType.Mongo.name());
        configuration.setEventTableName("event_stream");
        configuration.setPublishedTableName("published_version");
        configuration.setEventVersionUkName("aggregateRootId_1_version_1");
        configuration.setEventCommandIdUkName("aggregateRootId_1_commandId_1");
        configuration.setPublishedUkName("processorName_1_aggregateRootId_1");
        return configuration;
    }

    public static EventStoreConfiguration pg() {
        EventStoreConfiguration configuration = new EventStoreConfiguration();
        configuration.setDbName("enode");
        configuration.setDbType(DbType.Pg.name());
        configuration.setEventTableName("event_stream");
        configuration.setPublishedTableName("published_version");
        configuration.setEventVersionUkName("uk_aggregate_root_id_version");
        configuration.setEventCommandIdUkName("uk_aggregate_root_id_command_id");
        configuration.setPublishedUkName("uk_processor_name_aggregate_root_id");
        return configuration;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getEventTableName() {
        return eventTableName;
    }

    public void setEventTableName(String eventTableName) {
        this.eventTableName = eventTableName;
    }

    public String getPublishedTableName() {
        return publishedTableName;
    }

    public void setPublishedTableName(String publishedTableName) {
        this.publishedTableName = publishedTableName;
    }

    public String getEventVersionUkName() {
        return eventVersionUkName;
    }

    public void setEventVersionUkName(String eventVersionUkName) {
        this.eventVersionUkName = eventVersionUkName;
    }

    public String getEventCommandIdUkName() {
        return eventCommandIdUkName;
    }

    public void setEventCommandIdUkName(String eventCommandIdUkName) {
        this.eventCommandIdUkName = eventCommandIdUkName;
    }

    public String getPublishedUkName() {
        return publishedUkName;
    }

    public void setPublishedUkName(String publishedUkName) {
        this.publishedUkName = publishedUkName;
    }
}
