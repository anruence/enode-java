package com.qianzhui.enode.rocketmq.consumer;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.hook.ConsumeMessageContext;
import com.alibaba.rocketmq.client.impl.consumer.ConsumeMessageService;
import com.alibaba.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import com.alibaba.rocketmq.client.impl.consumer.ProcessQueue;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.stat.ConsumerStatsManager;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.common.message.MessageConst;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.body.CMResult;
import com.alibaba.rocketmq.common.protocol.body.ConsumeMessageDirectlyResult;
import com.alibaba.rocketmq.remoting.common.RemotingHelper;
import com.qianzhui.enode.rocketmq.consumer.listener.CompletableConsumeConcurrentlyContext;
import com.qianzhui.enode.rocketmq.consumer.listener.CompletableMessageListenerConcurrently;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CompletableConsumeMessageConcurrentlyService implements ConsumeMessageService {
    private static final Logger log = ClientLogger.getLog();
    private final DefaultMQPushConsumerImpl defaultMQPushConsumerImpl;
    private final DefaultMQPushConsumer defaultMQPushConsumer;
    private final CompletableMessageListenerConcurrently messageListener;
    private final BlockingQueue<Runnable> consumeRequestQueue;
    private final ThreadPoolExecutor consumeExecutor;
    private final String consumerGroup;

    private final ScheduledExecutorService scheduledExecutorService;


    public CompletableConsumeMessageConcurrentlyService(DefaultMQPushConsumerImpl defaultMQPushConsumerImpl,
                                                        CompletableMessageListenerConcurrently messageListener) {
        this.defaultMQPushConsumerImpl = defaultMQPushConsumerImpl;
        this.messageListener = messageListener;

        this.defaultMQPushConsumer = this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer();
        this.consumerGroup = this.defaultMQPushConsumer.getConsumerGroup();
        this.consumeRequestQueue = new LinkedBlockingQueue<Runnable>();

        this.consumeExecutor = new ThreadPoolExecutor(
                this.defaultMQPushConsumer.getConsumeThreadMin(),
                this.defaultMQPushConsumer.getConsumeThreadMax(),
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.consumeRequestQueue,
                new ThreadFactoryImpl("ConsumeMessageThread_"));

        this.scheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl(
                        "ConsumeMessageScheduledThread_"));
    }


    @Override
    public void start() {
    }


    @Override
    public void shutdown() {
        this.scheduledExecutorService.shutdown();
        this.consumeExecutor.shutdown();
    }


    public ConsumerStatsManager getConsumerStatsManager() {
        return this.defaultMQPushConsumerImpl.getConsumerStatsManager();
    }

    class ConsumeRequest implements Runnable {
        private final List<MessageExt> msgs;
        private final ProcessQueue processQueue;
        private final MessageQueue messageQueue;


        public ConsumeRequest(List<MessageExt> msgs, ProcessQueue processQueue, MessageQueue messageQueue) {
            this.msgs = msgs;
            this.processQueue = processQueue;
            this.messageQueue = messageQueue;
        }

        @Override
        public void run() {
            if (this.processQueue.isDropped()) {
                log.info("the message queue not be able to consume, because it's dropped {}", this.messageQueue);
                return;
            }

            CompletableFuture<ConsumeConcurrentlyStatus> statusFuture = new CompletableFuture<>();
            CompletableConsumeConcurrentlyContext context = new CompletableConsumeConcurrentlyContext(messageQueue, statusFuture);
            final ConsumeMessageContext consumeMessageContext = new ConsumeMessageContext();
            if (CompletableConsumeMessageConcurrentlyService.this.defaultMQPushConsumerImpl.hasHook()) {
                consumeMessageContext.setConsumerGroup(CompletableConsumeMessageConcurrentlyService.this.defaultMQPushConsumer.getConsumerGroup());
                consumeMessageContext.setProps(new HashMap<>());
                consumeMessageContext.setMq(messageQueue);
                consumeMessageContext.setMsgList(msgs);
                consumeMessageContext.setSuccess(false);
                CompletableConsumeMessageConcurrentlyService.this.defaultMQPushConsumerImpl
                        .executeHookBefore(consumeMessageContext);
            }

            long beginTimestamp = System.currentTimeMillis();

            try {
                CompletableConsumeMessageConcurrentlyService.this.resetRetryTopic(msgs);
                CompletableConsumeMessageConcurrentlyService.this.messageListener.consumeMessage(Collections.unmodifiableList(msgs), context);
            } catch (Throwable e) {
                log.warn("consumeMessage exception: {} Group: {} Msgs: {} MQ: {}",
                        RemotingHelper.exceptionSimpleDesc(e), CompletableConsumeMessageConcurrentlyService.this.consumerGroup, msgs, messageQueue);
            }

            statusFuture.handle((status, e) -> {
                if (e != null) {
                    log.warn("consumeMessage exception: {} Group: {} Msgs: {} MQ: {}",
                            RemotingHelper.exceptionSimpleDesc(e),
                            CompletableConsumeMessageConcurrentlyService.this.consumerGroup,
                            msgs,
                            messageQueue);
                }

                long consumeRT = System.currentTimeMillis() - beginTimestamp;

                if (null == status) {
                    log.warn("consumeMessage return null, Group: {} Msgs: {} MQ: {}",
                            CompletableConsumeMessageConcurrentlyService.this.consumerGroup,
                            msgs,
                            messageQueue);
                    status = ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                if (CompletableConsumeMessageConcurrentlyService.this.defaultMQPushConsumerImpl.hasHook()) {
                    consumeMessageContext.setStatus(status.toString());
                    consumeMessageContext.setSuccess(ConsumeConcurrentlyStatus.CONSUME_SUCCESS == status);
                    CompletableConsumeMessageConcurrentlyService.this.defaultMQPushConsumerImpl
                            .executeHookAfter(consumeMessageContext);
                }

                CompletableConsumeMessageConcurrentlyService.this.getConsumerStatsManager().incConsumeRT(
                        CompletableConsumeMessageConcurrentlyService.this.consumerGroup, messageQueue.getTopic(), consumeRT);

                if (!processQueue.isDropped()) {
                    CompletableConsumeMessageConcurrentlyService.this.processConsumeResult(status, context, this);
                } else {
                    log.warn("processQueue is dropped without process consume result. messageQueue={}, msgTreeMap={}, msgs={}", messageQueue, processQueue.getMsgTreeMap(), msgs);
                }

                return null;
            });
        }

        public List<MessageExt> getMsgs() {
            return msgs;
        }


        public ProcessQueue getProcessQueue() {
            return processQueue;
        }


        public MessageQueue getMessageQueue() {
            return messageQueue;
        }
    }


    public boolean sendMessageBack(final MessageExt msg, final CompletableConsumeConcurrentlyContext context) {
        int delayLevel = context.getDelayLevelWhenNextConsume();

        try {
            this.defaultMQPushConsumerImpl.sendMessageBack(msg, delayLevel, context.getMessageQueue().getBrokerName());
            return true;
        } catch (Exception e) {
            log.error("sendMessageBack exception, group: " + this.consumerGroup + " msg: " + msg.toString(), e);
        }

        return false;
    }


    public void processConsumeResult(
            final ConsumeConcurrentlyStatus status,
            final CompletableConsumeConcurrentlyContext context,
            final ConsumeRequest consumeRequest) {
        int ackIndex = context.getAckIndex();

        if (consumeRequest.getMsgs().isEmpty()) {
            return;
        }

        switch (status) {
            case CONSUME_SUCCESS:
                if (ackIndex >= consumeRequest.getMsgs().size()) {
                    ackIndex = consumeRequest.getMsgs().size() - 1;
                }
                int ok = ackIndex + 1;
                int failed = consumeRequest.getMsgs().size() - ok;
                this.getConsumerStatsManager().incConsumeOKTPS(consumerGroup,
                        consumeRequest.getMessageQueue().getTopic(), ok);
                this.getConsumerStatsManager().incConsumeFailedTPS(consumerGroup,
                        consumeRequest.getMessageQueue().getTopic(), failed);
                break;
            case RECONSUME_LATER:
                ackIndex = -1;
                this.getConsumerStatsManager().incConsumeFailedTPS(consumerGroup,
                        consumeRequest.getMessageQueue().getTopic(), consumeRequest.getMsgs().size());
                break;
            default:
                break;
        }

        switch (this.defaultMQPushConsumer.getMessageModel()) {
            case BROADCASTING:
                for (int i = ackIndex + 1; i < consumeRequest.getMsgs().size(); i++) {
                    MessageExt msg = consumeRequest.getMsgs().get(i);
                    log.warn("BROADCASTING, the message consume failed, drop it, {}", msg.toString());
                }
                break;
            case CLUSTERING:
                List<MessageExt> msgBackFailed = new ArrayList<MessageExt>(consumeRequest.getMsgs().size());
                for (int i = ackIndex + 1; i < consumeRequest.getMsgs().size(); i++) {
                    MessageExt msg = consumeRequest.getMsgs().get(i);
                    boolean result = this.sendMessageBack(msg, context);
                    if (!result) {
                        msg.setReconsumeTimes(msg.getReconsumeTimes() + 1);
                        msgBackFailed.add(msg);
                    }
                }

                if (!msgBackFailed.isEmpty()) {
                    consumeRequest.getMsgs().removeAll(msgBackFailed);
                    this.submitConsumeRequestLater(
                            msgBackFailed, consumeRequest.getProcessQueue(), consumeRequest.getMessageQueue());
                }
                break;
            default:
                break;
        }

        long offset = consumeRequest.getProcessQueue().removeMessage(consumeRequest.getMsgs());
        if (offset >= 0) {
            this.defaultMQPushConsumerImpl.getOffsetStore().updateOffset(consumeRequest.getMessageQueue(), offset, true);
        }
    }


    private void submitConsumeRequestLater(final List<MessageExt> msgs, final ProcessQueue processQueue, final MessageQueue messageQueue) {
        this.scheduledExecutorService.schedule(() -> CompletableConsumeMessageConcurrentlyService.this.submitConsumeRequest(msgs, processQueue, messageQueue, true), 5000, TimeUnit.MILLISECONDS);
    }


    @Override
    public void submitConsumeRequest(
            final List<MessageExt> msgs,
            final ProcessQueue processQueue,
            final MessageQueue messageQueue,
            final boolean dispatchToConsume) {
        final int consumeBatchSize = this.defaultMQPushConsumer.getConsumeMessageBatchMaxSize();
        if (msgs.size() <= consumeBatchSize) {
            ConsumeRequest consumeRequest = new ConsumeRequest(msgs, processQueue, messageQueue);
            this.consumeExecutor.submit(consumeRequest);
        } else {
            for (int total = 0; total < msgs.size(); ) {
                List<MessageExt> msgThis = new ArrayList<MessageExt>(consumeBatchSize);
                for (int i = 0; i < consumeBatchSize; i++, total++) {
                    if (total < msgs.size()) {
                        msgThis.add(msgs.get(total));
                    } else {
                        break;
                    }
                }

                ConsumeRequest consumeRequest = new ConsumeRequest(msgThis, processQueue, messageQueue);
                this.consumeExecutor.submit(consumeRequest);
            }
        }
    }


    @Override
    public void updateCorePoolSize(int corePoolSize) {
        if (corePoolSize > 0
                && corePoolSize <= Short.MAX_VALUE
                && corePoolSize < this.defaultMQPushConsumer.getConsumeThreadMax()) {
            this.consumeExecutor.setCorePoolSize(corePoolSize);
        }
    }


    @Override
    public void incCorePoolSize() {
        long corePoolSize = this.consumeExecutor.getCorePoolSize();
        if (corePoolSize < this.defaultMQPushConsumer.getConsumeThreadMax()) {
            this.consumeExecutor.setCorePoolSize(this.consumeExecutor.getCorePoolSize() + 1);
        }

        log.info("incCorePoolSize Concurrently from {} to {}, ConsumerGroup: {}",
                corePoolSize,
                this.consumeExecutor.getCorePoolSize(),
                this.consumerGroup);
    }


    @Override
    public void decCorePoolSize() {
        long corePoolSize = this.consumeExecutor.getCorePoolSize();
        if (corePoolSize > this.defaultMQPushConsumer.getConsumeThreadMin()) {
            this.consumeExecutor.setCorePoolSize(this.consumeExecutor.getCorePoolSize() - 1);
        }

        log.info("decCorePoolSize Concurrently from {} to {}, ConsumerGroup: {}",
                corePoolSize,
                this.consumeExecutor.getCorePoolSize(),
                this.consumerGroup);
    }


    @Override
    public int getCorePoolSize() {
        return this.consumeExecutor.getCorePoolSize();
    }


    @Override
    public ConsumeMessageDirectlyResult consumeMessageDirectly(MessageExt msg, String brokerName) {
        log.info("============ consumer message directly ============");
        ConsumeMessageDirectlyResult result = new ConsumeMessageDirectlyResult();
        result.setOrder(false);
        result.setAutoCommit(true);

        List<MessageExt> msgs = new ArrayList<MessageExt>();
        msgs.add(msg);
        MessageQueue mq = new MessageQueue();
        mq.setBrokerName(brokerName);
        mq.setTopic(msg.getTopic());
        mq.setQueueId(msg.getQueueId());

        CompletableFuture<ConsumeConcurrentlyStatus> consumeFuture = new CompletableFuture<>();
        CompletableConsumeConcurrentlyContext context = new CompletableConsumeConcurrentlyContext(mq, consumeFuture);

        this.resetRetryTopic(msgs);

        final long beginTime = System.currentTimeMillis();

        log.info("consumeMessageDirectly receive new messge: {}", msg);

        try {
            this.messageListener.consumeMessage(msgs, context);
            ConsumeConcurrentlyStatus status = consumeFuture.get(15, TimeUnit.SECONDS);
            if (status != null) {
                switch (status) {
                    case CONSUME_SUCCESS:
                        result.setConsumeResult(CMResult.CR_SUCCESS);
                        break;
                    case RECONSUME_LATER:
                        result.setConsumeResult(CMResult.CR_LATER);
                        break;
                    default:
                        break;
                }
            } else {
                result.setConsumeResult(CMResult.CR_RETURN_NULL);
            }
        } catch (Throwable e) {
            result.setConsumeResult(CMResult.CR_THROW_EXCEPTION);
            result.setRemark(RemotingHelper.exceptionSimpleDesc(e));

            log.warn(String.format("consumeMessageDirectly exception: %s Group: %s Msgs: %s MQ: %s",
                    RemotingHelper.exceptionSimpleDesc(e),
                    CompletableConsumeMessageConcurrentlyService.this.consumerGroup,
                    msgs,
                    mq), e);
        }

        result.setSpentTimeMills(System.currentTimeMillis() - beginTime);

        log.info("consumeMessageDirectly Result: {}", result);

        return result;
    }


    public void resetRetryTopic(final List<MessageExt> msgs) {
        final String groupTopic = MixAll.getRetryTopic(consumerGroup);
        for (MessageExt msg : msgs) {
            String retryTopic = msg.getProperty(MessageConst.PROPERTY_RETRY_TOPIC);
            if (retryTopic != null && groupTopic.equals(msg.getTopic())) {
                msg.setTopic(retryTopic);
            }
        }
    }
}
