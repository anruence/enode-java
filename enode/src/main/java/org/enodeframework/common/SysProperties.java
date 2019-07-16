package org.enodeframework.common;

/**
 * @author anruence@gmail.com
 */
public class SysProperties {
    /**
     * 聚合根方法执行时声明的name
     */
    public static final String AGGREGATE_ROOT_HANDLE_METHOD_NAME = "handle";

    public static final String ITEMS_COMMAND_RESULT_KEY = "COMMAND_RESULT";

    public static final String ITEMS_COMMAND_REPLY_ADDRESS_KEY = "COMMAND_REPLY_ADDRESS";

    /**
     * socket server RecordParser 使用的分隔符
     */
    public static final String DELIMITED = "**|**";

    /**
     * 默认等待执行结果的超时时间，10s
     */
    public static final int COMPLETION_SOURCE_TIMEOUT = 10 * 1000;

    /**
     * 接收Command执行结果服务启动时默认的端口
     */
    public static final int PORT = 2019;
}
