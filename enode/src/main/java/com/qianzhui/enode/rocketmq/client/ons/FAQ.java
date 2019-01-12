package com.qianzhui.enode.rocketmq.client.ons;

public class FAQ {
    public static final String FIND_NS_FAILED = "https://github.com/alibaba/ons/issues/1";

    public static final String CONNECT_BROKER_FAILED = "https://github.com/alibaba/ons/issues/2";

    public static final String SEND_MSG_TO_BROKER_TIMEOUT = "https://github.com/alibaba/ons/issues/3";

    public static final String SERVICE_STATE_WRONG = "https://github.com/alibaba/ons/issues/4";

    public static final String BROKER_RESPONSE_EXCEPTION = "https://github.com/alibaba/ons/issues/5";

    public static final String CLIENT_CHECK_MSG_EXCEPTION = "https://github.com/alibaba/ons/issues/6";

    public static final String TOPIC_ROUTE_NOT_EXIST = "https://github.com/alibaba/ons/issues/7";


    public static String errorMessage(final String msg, final String url) {
        return String.format("%s\nSee %s for further details.", msg, url);
    }
}