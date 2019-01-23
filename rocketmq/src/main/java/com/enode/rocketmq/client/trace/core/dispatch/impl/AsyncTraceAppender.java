package com.enode.rocketmq.client.trace.core.dispatch.impl;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.namesrv.TopAddressing;
import com.enode.rocketmq.client.ons.ClientRPCHook;
import com.enode.rocketmq.client.ons.SessionCredentials;
import com.enode.rocketmq.client.trace.core.common.OnsTraceConstants;
import com.enode.rocketmq.client.trace.core.common.OnsTraceContext;
import com.enode.rocketmq.client.trace.core.common.OnsTraceDataEncoder;
import com.enode.rocketmq.client.trace.core.common.OnsTraceTransferBean;
import com.enode.rocketmq.client.trace.core.dispatch.AsyncAppender;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class AsyncTraceAppender extends AsyncAppender {
    private final static Logger clientlog = ClientLogger.getLog();
    /**
     * 最大batch大小
     */
    private final int batchSize;
    /**
     * 消息轨迹数据的producer
     */
    private final DefaultMQProducer traceProducer;
    /**
     * 临时存储batch的数据
     */
    private List<OnsTraceTransferBean> transDataList;
    /**
     * 当前region
     */
    private String currentRegionId;
    /**
     * 发送缓冲区
     */
    private StringBuilder buffer;

    /**
     * 构造消息类型的轨迹数据发送器
     *
     * @param properties 参数属性
     * @throws MQClientException
     */
    public AsyncTraceAppender(Properties properties) throws MQClientException {
        buffer = new StringBuilder(10240);
        transDataList = new ArrayList<>();
        SessionCredentials sessionCredentials = new SessionCredentials();
        Properties sessionProperties = new Properties();
        sessionProperties.put("AccessKey", properties.getProperty(OnsTraceConstants.AccessKey));
        sessionProperties.put("SecretKey", properties.getProperty(OnsTraceConstants.SecretKey));
        sessionCredentials.updateContent(sessionProperties);
        traceProducer = new DefaultMQProducer(new ClientRPCHook(sessionCredentials));

        this.traceProducer.setProducerGroup(OnsTraceConstants.groupName);
        traceProducer.setSendMsgTimeout(5000);
        traceProducer.setInstanceName(properties.getProperty(OnsTraceConstants.InstanceName, String.valueOf(System.currentTimeMillis())));

        String nameSrv = properties.getProperty(OnsTraceConstants.NAMESRV_ADDR);
        if (nameSrv == null) {
            TopAddressing topAddressing = new TopAddressing(properties.getProperty(OnsTraceConstants.ADDRSRV_URL));
            nameSrv = topAddressing.fetchNSAddr();
        }
        traceProducer.setNamesrvAddr(nameSrv);
        traceProducer.setVipChannelEnabled(false);
        // 消息最大大小128K
        int maxSize = Integer.parseInt(properties.getProperty(OnsTraceConstants.MaxMsgSize, "128000"));
        batchSize = Integer.parseInt(properties.getProperty(OnsTraceConstants.MaxBatchNum, "1"));
        traceProducer.setMaxMessageSize(maxSize - 10 * 1000);
        traceProducer.start();
    }

    /**
     * 往消息缓冲区编码轨迹数据
     *
     * @param context
     */
    @Override
    public void append(Object context) {
        OnsTraceContext traceContext = (OnsTraceContext) context;
        if (traceContext == null) {
            return;
        }
        currentRegionId = traceContext.getRegionId();
        OnsTraceTransferBean traceData = OnsTraceDataEncoder.encoderFromContextBean(traceContext);
        transDataList.add(traceData);
    }

    /**
     * 实际批量发送数据
     */
    @Override
    public void flush() {
        if (transDataList.size() == 0) {
            return;
        }
        int currentBatch = transDataList.size() > batchSize ? batchSize : batchSize;
        // 临时缓冲区
        buffer.delete(0, buffer.length());
        int count = 0;
        Set<String> keySet = new HashSet<String>();

        for (OnsTraceTransferBean bean : transDataList) {
            keySet.addAll(bean.getTransKey());
            buffer.append(bean.getTransData());
            count++;
            // 保证包的大小不要超过上限
            if (count >= currentBatch || buffer.length() >= traceProducer.getMaxMessageSize()) {
                sendTraceDataByMQ(keySet, buffer.toString());
                // 发送完成，清除临时缓冲区
                buffer.delete(0, buffer.length());
                keySet.clear();
                count = 0;
            }
        }
        if (count > 0) {
            sendTraceDataByMQ(keySet, buffer.toString());
        }
        this.transDataList.clear();
    }

    /**
     * 发送数据的接口
     *
     * @param keySet 本批次包含的keyset
     * @param data   本批次的轨迹数据
     */
    public void sendTraceDataByMQ(Set<String> keySet, String data) {
        String topic = OnsTraceConstants.traceTopic + currentRegionId;
        final Message message = new Message(topic, data.getBytes());
        message.setKeys(keySet);
        try {
            traceProducer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                }

                @Override
                public void onException(Throwable e) {
                    //todo 对于发送失败的数据，如何保存，保证所有轨迹数据都记录下来
                    clientlog.info("send trace data failed ,the msgidSet is" + message.getKeys());
                }
            }, 5000);
        } catch (Exception e) {
            clientlog.info("send trace data failed ,the msgidSet is" + message.getKeys());
        }
    }

}