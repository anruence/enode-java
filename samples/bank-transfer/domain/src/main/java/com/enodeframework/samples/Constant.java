package com.enodeframework.samples;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

public class Constant {

    public static String COMMAND_TOPIC = "CommandSample";

    public static String EVENT_TOPIC = "EventSample";

    public static String APPLICATION_TOPIC = "ApplicationSample";

    public static String EXCEPTION_TOPIC = "ExceptionSample";

    public static String NAMESRVADDR = "127.0.0.1:9876";

    public static String EVENT_PRODUCER_GROUP = "EventProducerGroup";

    public static String EVENT_CONSUMER_GROUP = "EventConsumerGroup";

    public static String COMMAND_PRODUCER_GROUP = "CommandProducerGroup";

    public static String COMMAND_CONSUMER_GROUP = "CommandConsumerGroup";
}
