package com.enode.rocketmq.client;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.protocol.ResponseCode;
import com.alibaba.rocketmq.remoting.exception.RemotingConnectException;
import com.alibaba.rocketmq.remoting.exception.RemotingTimeoutException;
import com.enode.rocketmq.client.ons.FAQ;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractProducer {
    protected final AtomicBoolean started = new AtomicBoolean(false);
    private final DefaultMQProducer defaultMQProducer;

    protected AbstractProducer(Properties properties, MQClientInitializer mqClientInitializer) {
        mqClientInitializer.init(properties);

        this.defaultMQProducer = initProducer(properties, mqClientInitializer);
    }

    abstract protected DefaultMQProducer initProducer(Properties properties, MQClientInitializer mqClientInitializer);

    public void start() {
        try {
            if (this.started.compareAndSet(false, true)) {
                this.defaultMQProducer.start();
            }
        } catch (Exception e) {
            throw new RocketMQClientException(e.getMessage());
        }
    }

    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.defaultMQProducer.shutdown();
        }
    }

    public SendResult send(Message message, MessageQueueSelector selector, Object arg) {
        this.checkONSServiceState();

        try {
            return this.defaultMQProducer.send(message, selector, arg);
        } catch (Exception e) {
//            log.error(String.format("Send message Exception, %s", message), e);
            this.checkProducerException(e, message);
            return null;
        }
    }

    public void send(final Message message, final MessageQueueSelector selector, final Object arg,
                     final SendCallback sendCallback) {
        this.checkONSServiceState();

        try {
            this.defaultMQProducer.send(message, selector, arg, sendCallback);
        } catch (Exception e) {
            this.checkProducerException(e, message);
        }
    }

    private void checkONSServiceState() {
        switch (this.defaultMQProducer.getDefaultMQProducerImpl().getServiceState()) {
            case CREATE_JUST:
                throw new RocketMQClientException(FAQ.errorMessage(String
                        .format("You do not have start the producer[" + UtilAll.getPid() + "], %s", this.defaultMQProducer
                                .getDefaultMQProducerImpl().getServiceState()), FAQ.SERVICE_STATE_WRONG));
            case SHUTDOWN_ALREADY:
                throw new RocketMQClientException(FAQ.errorMessage(String.format(
                        "Your producer has been shut down, %s", this.defaultMQProducer.getDefaultMQProducerImpl()
                                .getServiceState()), FAQ.SERVICE_STATE_WRONG));
            case START_FAILED:
                throw new RocketMQClientException(FAQ.errorMessage(String.format(
                        "When you start your service throws an exception, %s", this.defaultMQProducer
                                .getDefaultMQProducerImpl().getServiceState()), FAQ.SERVICE_STATE_WRONG));
            case RUNNING:
                break;
            default:
                break;
        }
    }

    private void checkProducerException(Exception e, Message message) {
        if (e instanceof MQClientException) {
            //
            if (e.getCause() != null) {
                // 无法连接Broker
                if (e.getCause() instanceof RemotingConnectException) {
                    throw new RocketMQClientException(FAQ.errorMessage(
                            String.format("Connect broker failed, Topic: %s", message.getTopic()),
                            FAQ.CONNECT_BROKER_FAILED));
                }
                // 发送消息超时
                else if (e.getCause() instanceof RemotingTimeoutException) {
                    throw new RocketMQClientException(FAQ.errorMessage(String.format(
                            "Send message to broker timeout, %dms, Topic: %s",
                            this.defaultMQProducer.getSendMsgTimeout(), message.getTopic()),
                            FAQ.SEND_MSG_TO_BROKER_TIMEOUT));
                }
                // Broker返回异常
                else if (e.getCause() instanceof MQBrokerException) {
                    MQBrokerException excep = (MQBrokerException) e.getCause();
                    throw new RocketMQClientException(FAQ.errorMessage(
                            String.format("Receive a broker exception, Topic: %s, %s", message.getTopic(),
                                    excep.getErrorMessage()), FAQ.BROKER_RESPONSE_EXCEPTION));
                }
            }
            // 纯客户端异常
            else {
                MQClientException excep = (MQClientException) e;
                if (-1 == excep.getResponseCode()) {
                    throw new RocketMQClientException(FAQ.errorMessage(
                            String.format("Topic does not exist, Topic: %s, %s", message.getTopic(),
                                    excep.getErrorMessage()), FAQ.TOPIC_ROUTE_NOT_EXIST));
                } else if (ResponseCode.MESSAGE_ILLEGAL == excep.getResponseCode()) {
                    throw new RocketMQClientException(FAQ.errorMessage(String.format(
                            "ONS Client check message exception, Topic: %s, %s", message.getTopic(),
                            excep.getErrorMessage()), FAQ.CLIENT_CHECK_MSG_EXCEPTION));
                }
            }
        }

        throw new RocketMQClientException("defaultMQProducer send exception", e);
    }
}
