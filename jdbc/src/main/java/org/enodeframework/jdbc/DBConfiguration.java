package org.enodeframework.jdbc;

/**
 * @author anruence@gmail.com
 */
public class DBConfiguration {
    /**
     * 唯一键冲突时的错误码
     */
    private String sqlState;
    /**
     * 事件表的默认名称；默认为：event_stream
     */
    private String eventTableName;
    /**
     * 事件表的默认个数，用于支持最简易的单库分表；默认为：1，即不分表
     */
    private int eventTableCount;
    /**
     * 聚合根已发布事件表的默认名称；默认为：published_version
     */
    private String publishedVersionTableName;
    /**
     * 事件表的聚合根版本唯一索引的默认名称；默认为：uk_aggregate_root_id_version
     */
    private String eventTableVersionUniqueIndexName;
    /**
     * 事件表的聚合根已处理命令唯一索引的默认名称；默认为：uk_aggregate_root_id_command_id
     */
    private String eventTableCommandIdUniqueIndexName;
    /**
     * 聚合根已发布事件表的聚合根已发布版本唯一索引的默认名称；默认为：uk_processor_name_aggregate_root_id_version
     */
    private String publishedVersionUniqueIndexName;

    public DBConfiguration() {
        eventTableName = "event_stream";
        eventTableCount = 1;
        publishedVersionTableName = "published_version";
        eventTableVersionUniqueIndexName = "uk_aggregate_root_id_version";
        eventTableCommandIdUniqueIndexName = "uk_aggregate_root_id_command_id";
        publishedVersionUniqueIndexName = "uk_processor_name_aggregate_root_id_version";
        sqlState = "23000";
    }

    public static DBConfiguration mysql() {
        return new DBConfiguration();
    }

    public static DBConfiguration tidb() {
        return new DBConfiguration();
    }

    public static DBConfiguration postgresql() {
        DBConfiguration setting = new DBConfiguration();
        setting.setSqlState("23505");
        return setting;
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

    public String getPublishedVersionTableName() {
        return publishedVersionTableName;
    }

    public void setPublishedVersionTableName(String publishedVersionTableName) {
        this.publishedVersionTableName = publishedVersionTableName;
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

    public String getSqlState() {
        return sqlState;
    }

    public void setSqlState(String sqlState) {
        this.sqlState = sqlState;
    }
}
