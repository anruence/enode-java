package com.qianzhui.enode.rocketmq.client.ons;

import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enode.rocketmq.consumer.CompletableDefaultMQPushConsumer;
import com.qianzhui.enode.rocketmq.client.AbstractConsumer;
import com.qianzhui.enode.rocketmq.client.Consumer;
import com.qianzhui.enode.rocketmq.client.MQClientInitializer;
import com.qianzhui.enode.rocketmq.client.RocketMQClientException;
import com.qianzhui.enode.rocketmq.trace.core.common.OnsTraceConstants;
import com.qianzhui.enode.rocketmq.trace.core.dispatch.AsyncDispatcher;
import com.qianzhui.enode.rocketmq.trace.core.dispatch.impl.AsyncArrayDispatcher;
import com.qianzhui.enode.rocketmq.trace.core.utils.OnsConsumeMessageHookImpl;
import org.slf4j.Logger;

import java.util.Properties;

public class ONSConsumerImpl extends AbstractConsumer implements Consumer {
    private static final Logger logger = ENodeLogger.getLog();

    protected AsyncDispatcher traceDispatcher;

    public ONSConsumerImpl(Properties properties) {
        super(properties, new ONSClientInitializer());
    }

    @Override
    protected CompletableDefaultMQPushConsumer initConsumer(Properties properties, MQClientInitializer mqClientInitializer) {
        SessionCredentials sessionCredentials = ((ONSClientInitializer) mqClientInitializer).sessionCredentials;
        //DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(new ClientRPCHook(((ONSClientInitializer) mqClientInitializer).sessionCredentials));
        CompletableDefaultMQPushConsumer defaultMQPushConsumer = new CompletableDefaultMQPushConsumer(new ClientRPCHook(sessionCredentials));

        String consumerGroup = properties.getProperty(PropertyKeyConst.ConsumerId);
        if (null == consumerGroup) {
            throw new RocketMQClientException("\'ConsumerId\' property is null");
        }

        boolean isVipChannelEnabled = Boolean.parseBoolean(properties.getProperty(PropertyKeyConst.isVipChannelEnabled, "false"));
        defaultMQPushConsumer.setVipChannelEnabled(isVipChannelEnabled);

        String messageModel =
                properties.getProperty(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);
        defaultMQPushConsumer.setMessageModel(MessageModel.valueOf(messageModel));
        defaultMQPushConsumer.setConsumerGroup(consumerGroup);
        defaultMQPushConsumer.setInstanceName(mqClientInitializer.buildIntanceName());
        defaultMQPushConsumer.setNamesrvAddr(mqClientInitializer.getNameServerAddr());

        if (properties.containsKey(PropertyKeyConst.ConsumeThreadNums)) {
            defaultMQPushConsumer.setConsumeThreadMin(Integer.valueOf(properties.get(
                    PropertyKeyConst.ConsumeThreadNums).toString()));

            defaultMQPushConsumer.setConsumeThreadMax(Integer.valueOf(properties.get(
                    PropertyKeyConst.ConsumeThreadNums).toString()));
        }

        // 为Consumer增加消息轨迹回发模块
        try {
            Properties tempProperties = new Properties();
            tempProperties.put(OnsTraceConstants.AccessKey, sessionCredentials.getAccessKey());
            tempProperties.put(OnsTraceConstants.SecretKey, sessionCredentials.getSecretKey());
            tempProperties.put(OnsTraceConstants.MaxMsgSize, "128000");
            tempProperties.put(OnsTraceConstants.AsyncBufferSize, "2048");
            tempProperties.put(OnsTraceConstants.MaxBatchNum, "100");
            tempProperties.put(OnsTraceConstants.NAMESRV_ADDR, mqClientInitializer.getNameServerAddr());
            tempProperties.put(OnsTraceConstants.InstanceName, mqClientInitializer.buildIntanceName());
            traceDispatcher = new AsyncArrayDispatcher(tempProperties);
            traceDispatcher.start(defaultMQPushConsumer.getInstanceName());
            defaultMQPushConsumer.getDefaultMQPushConsumerImpl().registerConsumeMessageHook(
                    new OnsConsumeMessageHookImpl(traceDispatcher));
        } catch (Throwable e) {
            logger.error("system mqtrace hook init failed ,maybe can't send msg trace data");
        }

        return defaultMQPushConsumer;
    }

    @Override
    public void shutdown() {
        super.shutdown();

        if (null != traceDispatcher) {
            traceDispatcher.shutdown();
        }
    }
}
