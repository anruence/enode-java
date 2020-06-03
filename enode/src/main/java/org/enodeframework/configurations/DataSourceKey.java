package org.enodeframework.configurations;

/**
 * @author anruence@gmail.com
 */
public class DataSourceKey {
    /**
     * 命令表的默认名称；默认为：Command
     */
    public static String COMMAND_TABLE_NAME = "COMMAND_TABLE_NAME";
    /**
     * 事件表的默认名称；默认为：EventStream
     */
    public static String EVENT_TABLE_NAME = "EVENT_TABLE_NAME";
    /**
     * 事件表的默认个数，用于支持最简易的单库分表；默认为：1，即不分表
     */
    public static String EVENT_TABLE_COUNT = "EVENT_TABLE_COUNT";
    /**
     * mysql 唯一键冲突时的错误码
     */
    public static String MYSQL_DUPLICATE_CODE = "MYSQL_DUPLICATE_CODE";
    /**
     * 事件表批量持久化单批最大事件数；默认为：1000
     */
    public static String EVENT_TABLE_BULKCOPY_BATCHSIZE = "EVENT_TABLE_BULKCOPY_BATCHSIZE";
    /**
     * 事件表批量持久化单批超时时间；单位为秒，默认为：60s
     */
    public static String EVENT_TABLE_BULKCOPY_TIMEOUT = "EVENT_TABLE_BULKCOPY_TIMEOUT";
    /**
     * 聚合根已发布事件表的默认名称；默认为：PublishedVersion
     */
    public static String PUBLISHED_VERSION_TABLENAME = "PUBLISHED_VERSION_TABLENAME";
    /**
     * Command表的CommandId的唯一索引的默认名称；默认为：IX_Command_CommandId
     */
    public static String COMMAND_TABLE_COMMANDID_UNIQUE_INDEX_NAME = "COMMAND_TABLE_COMMANDID_UNIQUE_INDEX_NAME";
    /**
     * 事件表的聚合根版本唯一索引的默认名称；默认为：IX_EventStream_AggId_Version
     */
    public static String EVENT_TABLE_VERSION_UNIQUE_INDEX_NAME = "EVENT_TABLE_VERSION_UNIQUE_INDEX_NAME";
    /**
     * 聚合根已发布事件表的聚合根已发布版本唯一索引的默认名称；默认为：IX_PublishedVersion_AggId_Version
     */
    public static String PUBLISHED_VERSION_UNIQUE_INDEX_NAME = "PUBLISHED_VERSION_UNIQUE_INDEX_NAME";
}
