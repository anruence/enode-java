package org.enodeframework.common;

/**
 * @author anruence@gmail.com
 */
public class SysProperties {
    /**
     * 聚合根方法执行时声明的name
     */
    public static final String AGGREGATE_ROOT_HANDLE_METHOD_NAME = "handle";

    /**
     * socket server RecordParser 使用的分隔符
     */
    public static final String DELIMITED = "**|**";

    /**
     * 默认等待执行结果的超时时间，3分钟
     */
    public static final int COMPLETION_SOURCE_TIMEOUT = 3 * 60 * 1000;

    /**
     * 接收Command执行结果服务启动时默认的端口
     */
    public static final int PORT = 2019;
}
