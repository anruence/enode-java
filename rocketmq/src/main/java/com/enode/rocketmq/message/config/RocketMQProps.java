package com.enode.rocketmq.message.config;

import java.util.Properties;

public class RocketMQProps {

    private Properties producerProps;

    private Properties consumerProps;

    public Properties getProducerProps() {
        return producerProps;
    }

    public void setProducerProps(Properties producerProps) {
        this.producerProps = producerProps;
    }

    public Properties getConsumerProps() {
        return consumerProps;
    }

    public void setConsumerProps(Properties consumerProps) {
        this.consumerProps = consumerProps;
    }
}
