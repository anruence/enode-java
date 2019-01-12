package com.qianzhui.enode.common.rocketmq.consumer;

import com.alibaba.rocketmq.client.QueryResult;
import com.alibaba.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import com.alibaba.rocketmq.client.consumer.store.OffsetStore;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.remoting.RPCHook;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.qianzhui.enode.common.rocketmq.consumer.listener.CompletableMessageListenerConcurrently;
import com.qianzhui.enode.common.rocketmq.impl.consumer.CompletableDefaultMQPushConsumerImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CompletableDefaultMQPushConsumer extends com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer {

    protected final transient DefaultMQPushConsumerImpl defaultMQPushConsumerImpl;
    /**
     * Do the same thing for the same Group, the application must be set,and
     * guarantee Globally unique
     */
    private String consumerGroup;
    /**
     * Consumption pattern,default is clustering
     */
    private MessageModel messageModel = MessageModel.CLUSTERING;
    /**
     * Consumption offset
     */
    private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
    /**
     * Backtracking consumption time with second precision.time format is
     * 20131223171201<br>
     * Implying Seventeen twelve and 01 seconds on December 23, 2013 year<br>
     * Default backtracking consumption time Half an hour ago
     */
    private String consumeTimestamp = UtilAll.timeMillisToHumanString3(System.currentTimeMillis()
            - (1000 * 60 * 30));
    /**
     * Queue allocation algorithm
     */
    private AllocateMessageQueueStrategy allocateMessageQueueStrategy;

    /**
     * Subscription relationship
     */
    private Map<String /* topic */, String /* sub expression */> subscription = new HashMap<String, String>();
    /**
     * Message listener
     */
    private MessageListener messageListener;
    /**
     * Offset Storage
     */
    private OffsetStore offsetStore;
    /**
     * Minimum consumer thread number
     */
    private int consumeThreadMin = 20;
    /**
     * Max consumer thread number
     */
    private int consumeThreadMax = 64;

    /**
     * Threshold for dynamic adjustment of the number of thread pool
     */
    private long adjustThreadPoolNumsThreshold = 100000;

    /**
     * Concurrently max span offset.it has no effect on sequential consumption
     */
    private int consumeConcurrentlyMaxSpan = 2000;
    /**
     * Flow control threshold
     */
    private int pullThresholdForQueue = 1000;
    /**
     * Message pull Interval
     */
    private long pullInterval = 0;
    /**
     * Batch consumption size
     */
    private int consumeMessageBatchMaxSize = 1;
    /**
     * Batch pull size
     */
    private int pullBatchSize = 32;

    /**
     * Whether update subscription relationship when every pull
     */
    private boolean postSubscriptionWhenPull = false;

    /**
     * Whether the unit of subscription group
     */
    private boolean unitMode = false;


    public CompletableDefaultMQPushConsumer() {
        this(MixAll.DEFAULT_CONSUMER_GROUP, null, new AllocateMessageQueueAveragely());
    }


    public CompletableDefaultMQPushConsumer(RPCHook rpcHook) {
        this(MixAll.DEFAULT_CONSUMER_GROUP, rpcHook, new AllocateMessageQueueAveragely());
    }


    public CompletableDefaultMQPushConsumer(final String consumerGroup) {
        this(consumerGroup, null, new AllocateMessageQueueAveragely());
    }


    public CompletableDefaultMQPushConsumer(final String consumerGroup, RPCHook rpcHook,
                                            AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this.consumerGroup = consumerGroup;
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
        defaultMQPushConsumerImpl = new CompletableDefaultMQPushConsumerImpl(this, rpcHook);
    }


    @Override
    public void createTopic(String key, String newTopic, int queueNum) throws MQClientException {
        createTopic(key, newTopic, queueNum, 0);
    }


    @Override
    public void createTopic(String key, String newTopic, int queueNum, int topicSysFlag)
            throws MQClientException {
        this.defaultMQPushConsumerImpl.createTopic(key, newTopic, queueNum, topicSysFlag);
    }


    @Override
    public long searchOffset(MessageQueue mq, long timestamp) throws MQClientException {
        return this.defaultMQPushConsumerImpl.searchOffset(mq, timestamp);
    }


    @Override
    public long maxOffset(MessageQueue mq) throws MQClientException {
        return this.defaultMQPushConsumerImpl.maxOffset(mq);
    }


    @Override
    public long minOffset(MessageQueue mq) throws MQClientException {
        return this.defaultMQPushConsumerImpl.minOffset(mq);
    }


    @Override
    public long earliestMsgStoreTime(MessageQueue mq) throws MQClientException {
        return this.defaultMQPushConsumerImpl.earliestMsgStoreTime(mq);
    }


    @Override
    public MessageExt viewMessage(String msgId) throws RemotingException, MQBrokerException,
            InterruptedException, MQClientException {
        return this.defaultMQPushConsumerImpl.viewMessage(msgId);
    }


    @Override
    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end)
            throws MQClientException, InterruptedException {
        return this.defaultMQPushConsumerImpl.queryMessage(topic, key, maxNum, begin, end);
    }


    @Override
    public AllocateMessageQueueStrategy getAllocateMessageQueueStrategy() {
        return allocateMessageQueueStrategy;
    }


    @Override
    public void setAllocateMessageQueueStrategy(AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
    }


    @Override
    public int getConsumeConcurrentlyMaxSpan() {
        return consumeConcurrentlyMaxSpan;
    }


    @Override
    public void setConsumeConcurrentlyMaxSpan(int consumeConcurrentlyMaxSpan) {
        this.consumeConcurrentlyMaxSpan = consumeConcurrentlyMaxSpan;
    }


    @Override
    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }


    @Override
    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }


    @Override
    public int getConsumeMessageBatchMaxSize() {
        return consumeMessageBatchMaxSize;
    }


    @Override
    public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
        this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
    }


    @Override
    public String getConsumerGroup() {
        return consumerGroup;
    }


    @Override
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }


    @Override
    public int getConsumeThreadMax() {
        return consumeThreadMax;
    }


    @Override
    public void setConsumeThreadMax(int consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }


    @Override
    public int getConsumeThreadMin() {
        return consumeThreadMin;
    }


    @Override
    public void setConsumeThreadMin(int consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
    }


    @Override
    public DefaultMQPushConsumerImpl getDefaultMQPushConsumerImpl() {
        return defaultMQPushConsumerImpl;
    }


    @Override
    public MessageListener getMessageListener() {
        return messageListener;
    }


    @Override
    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }


    @Override
    public MessageModel getMessageModel() {
        return messageModel;
    }


    @Override
    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }


    @Override
    public int getPullBatchSize() {
        return pullBatchSize;
    }


    @Override
    public void setPullBatchSize(int pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
    }


    @Override
    public long getPullInterval() {
        return pullInterval;
    }


    @Override
    public void setPullInterval(long pullInterval) {
        this.pullInterval = pullInterval;
    }


    @Override
    public int getPullThresholdForQueue() {
        return pullThresholdForQueue;
    }


    @Override
    public void setPullThresholdForQueue(int pullThresholdForQueue) {
        this.pullThresholdForQueue = pullThresholdForQueue;
    }


    @Override
    public Map<String, String> getSubscription() {
        return subscription;
    }


    @Override
    public void setSubscription(Map<String, String> subscription) {
        this.subscription = subscription;
    }


    @Override
    public void sendMessageBack(MessageExt msg, int delayLevel) throws RemotingException, MQBrokerException,
            InterruptedException, MQClientException {
        this.defaultMQPushConsumerImpl.sendMessageBack(msg, delayLevel, null);
    }


    @Override
    public void sendMessageBack(MessageExt msg, int delayLevel, String brokerName) throws RemotingException,
            MQBrokerException, InterruptedException, MQClientException {
        this.defaultMQPushConsumerImpl.sendMessageBack(msg, delayLevel, brokerName);
    }


    @Override
    public Set<MessageQueue> fetchSubscribeMessageQueues(String topic) throws MQClientException {
        return this.defaultMQPushConsumerImpl.fetchSubscribeMessageQueues(topic);
    }


    @Override
    public void start() throws MQClientException {
        this.defaultMQPushConsumerImpl.start();
    }


    @Override
    public void shutdown() {
        this.defaultMQPushConsumerImpl.shutdown();
    }


    @Override
    public void registerMessageListener(MessageListenerConcurrently messageListener) {
        this.messageListener = messageListener;
        this.defaultMQPushConsumerImpl.registerMessageListener(messageListener);
    }


    @Override
    public void registerMessageListener(MessageListenerOrderly messageListener) {
        this.messageListener = messageListener;
        this.defaultMQPushConsumerImpl.registerMessageListener(messageListener);
    }

    public void registerMessageListener(CompletableMessageListenerConcurrently messageListener) {
        this.setMessageListener(messageListener);
        this.defaultMQPushConsumerImpl.registerMessageListener(messageListener);
    }

    @Override
    public void subscribe(String topic, String subExpression) throws MQClientException {
        this.defaultMQPushConsumerImpl.subscribe(topic, subExpression);
    }


    @Override
    public void subscribe(String topic, String fullClassName, String filterClassSource)
            throws MQClientException {
        this.defaultMQPushConsumerImpl.subscribe(topic, fullClassName, filterClassSource);
    }


    @Override
    public void unsubscribe(String topic) {
        this.defaultMQPushConsumerImpl.unsubscribe(topic);
    }


    @Override
    public void updateCorePoolSize(int corePoolSize) {
        this.defaultMQPushConsumerImpl.updateCorePoolSize(corePoolSize);
    }


    @Override
    public void suspend() {
        this.defaultMQPushConsumerImpl.suspend();
    }


    @Override
    public void resume() {
        this.defaultMQPushConsumerImpl.resume();
    }


    @Override
    public OffsetStore getOffsetStore() {
        return offsetStore;
    }


    @Override
    public void setOffsetStore(OffsetStore offsetStore) {
        this.offsetStore = offsetStore;
    }


    @Override
    public String getConsumeTimestamp() {
        return consumeTimestamp;
    }


    @Override
    public void setConsumeTimestamp(String consumeTimestamp) {
        this.consumeTimestamp = consumeTimestamp;
    }


    @Override
    public boolean isPostSubscriptionWhenPull() {
        return postSubscriptionWhenPull;
    }


    @Override
    public void setPostSubscriptionWhenPull(boolean postSubscriptionWhenPull) {
        this.postSubscriptionWhenPull = postSubscriptionWhenPull;
    }


    @Override
    public boolean isUnitMode() {
        return unitMode;
    }


    @Override
    public void setUnitMode(boolean isUnitMode) {
        this.unitMode = isUnitMode;
    }


    @Override
    public long getAdjustThreadPoolNumsThreshold() {
        return adjustThreadPoolNumsThreshold;
    }


    @Override
    public void setAdjustThreadPoolNumsThreshold(long adjustThreadPoolNumsThreshold) {
        this.adjustThreadPoolNumsThreshold = adjustThreadPoolNumsThreshold;
    }
}
