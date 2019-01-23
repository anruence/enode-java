package com.enode.rocketmq.client.ons;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.enode.common.logging.ENodeLogger;
import com.enode.rocketmq.client.AbstractProducer;
import com.enode.rocketmq.client.MQClientInitializer;
import com.enode.rocketmq.client.Producer;
import com.enode.rocketmq.client.trace.core.OnsClientSendMessageHookImpl;
import com.enode.rocketmq.client.trace.core.common.OnsTraceConstants;
import com.enode.rocketmq.client.trace.core.dispatch.AsyncDispatcher;
import com.enode.rocketmq.client.trace.core.dispatch.impl.AsyncArrayDispatcher;
import org.slf4j.Logger;

import java.util.Properties;

public class ONSProducerImpl extends AbstractProducer implements Producer {

    private static final Logger log = ENodeLogger.getLog();
    private AsyncDispatcher traceDispatcher;

    public ONSProducerImpl(final Properties properties) {
        super(properties, new ONSClientInitializer());
    }

    @Override
    protected DefaultMQProducer initProducer(Properties properties, MQClientInitializer clientInitializer) {
        SessionCredentials sessionCredentials = ((ONSClientInitializer) clientInitializer).sessionCredentials;
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer(new ClientRPCHook(sessionCredentials));

        String producerGroup =
                properties.getProperty(PropertyKeyConst.ProducerId, "__ONS_PRODUCER_DEFAULT_GROUP");
        defaultMQProducer.setProducerGroup(producerGroup);

        boolean isVipChannelEnabled = Boolean.parseBoolean(properties.getProperty("isVipChannelEnabled", "false"));
        defaultMQProducer.setVipChannelEnabled(isVipChannelEnabled);

        String sendMsgTimeoutMillis = properties.getProperty(PropertyKeyConst.SendMsgTimeoutMillis, "5000");
        defaultMQProducer.setSendMsgTimeout(Integer.parseInt(sendMsgTimeoutMillis));

        defaultMQProducer.setInstanceName(clientInitializer.buildIntanceName());
        defaultMQProducer.setNamesrvAddr(clientInitializer.getNameServerAddr());
        // 消息最大大小4M
        defaultMQProducer.setMaxMessageSize(1024 * 1024 * 4);

        // 为Producer增加消息轨迹回发模块
        try {
            Properties tempProperties = new Properties();
            tempProperties.put(OnsTraceConstants.AccessKey, sessionCredentials.getAccessKey());
            tempProperties.put(OnsTraceConstants.SecretKey, sessionCredentials.getSecretKey());
            tempProperties.put(OnsTraceConstants.MaxMsgSize, "128000");
            tempProperties.put(OnsTraceConstants.AsyncBufferSize, "2048");
            tempProperties.put(OnsTraceConstants.MaxBatchNum, "10");
            tempProperties.put(OnsTraceConstants.NAMESRV_ADDR, clientInitializer.getNameServerAddr());
            tempProperties.put(OnsTraceConstants.InstanceName, clientInitializer.buildIntanceName());
            traceDispatcher = new AsyncArrayDispatcher(tempProperties);
            traceDispatcher.start(defaultMQProducer.getInstanceName());
            defaultMQProducer.getDefaultMQProducerImpl().registerSendMessageHook(
                    new OnsClientSendMessageHookImpl(traceDispatcher));
        } catch (Throwable e) {
            log.error("system mqtrace hook init failed ,maybe can't send msg trace data");
        }

        return defaultMQProducer;
    }

    @Override
    public void shutdown() {
        super.shutdown();

        if (null != traceDispatcher) {
            traceDispatcher.shutdown();
        }
    }
}
