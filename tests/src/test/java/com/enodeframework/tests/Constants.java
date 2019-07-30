package com.enodeframework.tests;

public class Constants {
    public static String NAMESRVADDR = "p.anruence.com:9876";
//    public static String KAFKA_SERVER = "p.anruence.com:9092";
    public static String KAFKA_SERVER = "127.0.0.1:9092";
    //    public static String JDBC_URL = "jdbc:mysql://p.anruence.com:13306/enode";
    public static String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/enode";

    public static String COMMAND_TOPIC = "ENodeTestCommandTopic";
    public static String EVENT_TOPIC = "ENodeTestEventTopic";
    public static String APPLICATION_TOPIC = "ENodeTestApplicationMessageTopic";
    public static String EXCEPTION_TOPIC = "ENodeTestExceptionTopic";
    public static String DEFAULT_PRODUCER_GROUP = "DefaultProducerGroup";
    public static String DEFAULT_CONSUMER_GROUP = "DefaultConsumerGroup";
}
