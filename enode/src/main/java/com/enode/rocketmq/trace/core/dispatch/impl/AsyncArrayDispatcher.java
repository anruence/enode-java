package com.enode.rocketmq.trace.core.dispatch.impl;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.namesrv.TopAddressing;
import com.enode.rocketmq.client.ons.ClientRPCHook;
import com.enode.rocketmq.client.ons.SessionCredentials;
import com.enode.rocketmq.trace.core.common.OnsTraceConstants;
import com.enode.rocketmq.trace.core.common.OnsTraceContext;
import com.enode.rocketmq.trace.core.common.OnsTraceDataEncoder;
import com.enode.rocketmq.trace.core.common.OnsTraceTransferBean;
import com.enode.rocketmq.trace.core.dispatch.AsyncDispatcher;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncArrayDispatcher implements AsyncDispatcher {
    private final static Logger clientlog = ClientLogger.getLog();
    private final int queueSize;
    private final int batchSize;
    private final DefaultMQProducer traceProducer;
    private final ThreadPoolExecutor traceExecuter;
    // 最近丢弃的日志条数
    private AtomicLong discardCount;
    private Thread worker;
    private ArrayBlockingQueue<OnsTraceContext> traceContextQueue;
    private ArrayBlockingQueue<Runnable> appenderQueue;
    private volatile Thread shutDownHook;
    private volatile boolean stopped = false;

    public AsyncArrayDispatcher(Properties properties) throws MQClientException {
        int queueSize = Integer.parseInt(properties.getProperty(OnsTraceConstants.AsyncBufferSize, "2048"));
        // queueSize 取大于或等于 value 的 2 的 n 次方数
        queueSize = 1 << (32 - Integer.numberOfLeadingZeros(queueSize - 1));
        this.queueSize = queueSize;
        batchSize = Integer.parseInt(properties.getProperty(OnsTraceConstants.MaxBatchNum, "1"));
        this.discardCount = new AtomicLong(0L);
        traceContextQueue = new ArrayBlockingQueue<OnsTraceContext>(1024);
        appenderQueue = new ArrayBlockingQueue<Runnable>(queueSize);

        this.traceExecuter = new ThreadPoolExecutor(//
                10, //
                20, //
                1000 * 60, //
                TimeUnit.MILLISECONDS, //
                this.appenderQueue, //
                new ThreadFactoryImpl("MQTraceSendThread_"));

        SessionCredentials sessionCredentials = new SessionCredentials();
        Properties sessionProperties = new Properties();
        String accessKey = properties.getProperty(OnsTraceConstants.AccessKey);
        String secretKey = properties.getProperty(OnsTraceConstants.SecretKey);
        sessionProperties.put(OnsTraceConstants.AccessKey, accessKey);
        sessionProperties.put(OnsTraceConstants.SecretKey, secretKey);
        sessionCredentials.updateContent(sessionProperties);
        traceProducer = new DefaultMQProducer(new ClientRPCHook(sessionCredentials));

        this.traceProducer.setProducerGroup(accessKey + OnsTraceConstants.groupName);
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
        traceProducer.setMaxMessageSize(maxSize - 10 * 1000);
        traceProducer.start();
    }

    @Override
    public void start(final String workName) {
        this.worker = new Thread(new AsyncRunnable(), "MQ-AsyncArrayDispatcher-Thread-" + workName);
        this.worker.setDaemon(true);
        this.worker.start();
        this.registerShutDownHook();
    }

    @Override
    public boolean append(final Object ctx) {
        boolean result = traceContextQueue.offer((OnsTraceContext) ctx);
        if (!result) {
            clientlog.info("buffer full" + discardCount.incrementAndGet() + " ,context is " + ctx);
        }
        return result;
    }

    @Override
    public void flush() throws IOException {
        // 最多等待刷新的时间，避免数据一直在写导致无法返回
        long end = System.currentTimeMillis() + 500;
        while (traceContextQueue.size() > 0 || appenderQueue.size() > 0 && System.currentTimeMillis() <= end) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
        clientlog.info("------end trace send " + traceContextQueue.size() + "   " + appenderQueue.size());
    }

    @Override
    public void shutdown() {
        this.stopped = true;
        this.traceExecuter.shutdown();
        if (null != this.traceProducer) {
            traceProducer.shutdown();
        }
        this.removeShutdownHook();
    }

    public void registerShutDownHook() {
        if (shutDownHook == null) {
            shutDownHook = new Thread(new Runnable() {
                private volatile boolean hasShutdown = false;

                @Override
                public void run() {
                    synchronized (this) {
                        if (!this.hasShutdown) {
                            try {
                                flush();
                            } catch (IOException e) {
                                clientlog.error("system mqtrace hook shutdown failed ,maybe loss some trace data");
                            }
                        }
                    }
                }
            }, "ShutdownHookMQTrace");
            Runtime.getRuntime().addShutdownHook(shutDownHook);
        }
    }

    public void removeShutdownHook() {
        if (shutDownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutDownHook);
        }
    }

    class AsyncRunnable implements Runnable {
        private boolean stopped;

        @Override
        public void run() {
            while (!stopped) {
                List<OnsTraceContext> contexts = new ArrayList<OnsTraceContext>(batchSize);
                for (int i = 0; i < batchSize; i++) {
                    OnsTraceContext context = null;
                    try {
                        context = traceContextQueue.poll(5, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                    }
                    if (context != null) {
                        contexts.add(context);
                    } else {
                        break;
                    }
                }
                if (contexts.size() > 0) {
                    AsyncAppenderRequest request = new AsyncAppenderRequest(contexts);
                    traceExecuter.submit(request);
                } else if (AsyncArrayDispatcher.this.stopped) {
                    this.stopped = true;
                }
            }

        }
    }

    class AsyncAppenderRequest implements Runnable {
        List<OnsTraceContext> contextList;

        public AsyncAppenderRequest(final List<OnsTraceContext> contextList) {
            if (contextList != null) {
                this.contextList = contextList;
            } else {
                this.contextList = new ArrayList<OnsTraceContext>(1);
            }
        }

        @Override
        public void run() {
            sendTraceData(contextList);
        }

        /**
         * 往消息缓冲区编码轨迹数据
         *
         * @param contextList
         */
        public void sendTraceData(List<OnsTraceContext> contextList) {
            List<OnsTraceTransferBean> transBeanList = new ArrayList<OnsTraceTransferBean>();
            String currentRegionId = OnsTraceConstants.default_region;
            for (OnsTraceContext context : contextList) {
                currentRegionId = context.getRegionId();
                OnsTraceTransferBean traceData = OnsTraceDataEncoder.encoderFromContextBean(context);
                transBeanList.add(traceData);
            }
            flushData(transBeanList, currentRegionId);
        }

        /**
         * 实际批量发送数据
         */
        private void flushData(List<OnsTraceTransferBean> transBeanList, String currentRegionId) {
            if (transBeanList.size() == 0) {
                return;
            }
            // 临时缓冲区
            StringBuilder buffer = new StringBuilder(1024);
            int count = 0;
            Set<String> keySet = new HashSet<String>();

            for (OnsTraceTransferBean bean : transBeanList) {
                keySet.addAll(bean.getTransKey());
                buffer.append(bean.getTransData());
                count++;
                // 保证包的大小不要超过上限
                if (buffer.length() >= traceProducer.getMaxMessageSize()) {
                    sendTraceDataByMQ(keySet, buffer.toString(), currentRegionId);
                    // 发送完成，清除临时缓冲区
                    buffer.delete(0, buffer.length());
                    keySet.clear();
                    count = 0;
                }
            }
            if (count > 0) {
                sendTraceDataByMQ(keySet, buffer.toString(), currentRegionId);
            }
            transBeanList.clear();
        }

        /**
         * 发送数据的接口
         *
         * @param keySet 本批次包含的keyset
         * @param data   本批次的轨迹数据
         */
        private void sendTraceDataByMQ(Set<String> keySet, final String data, String currentRegionId) {
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
                        clientlog.info("send trace data ,the traceData is " + data);
                    }
                }, 5000);
            } catch (Exception e) {
                clientlog.info("send trace data,the traceData is" + data);
            }
        }
    }
}