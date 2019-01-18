package com.enode.rocketmq.trace.core;

import com.alibaba.rocketmq.client.hook.SendMessageContext;
import com.alibaba.rocketmq.client.hook.SendMessageHook;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.MixAll;
import com.enode.rocketmq.trace.core.common.OnsTraceBean;
import com.enode.rocketmq.trace.core.common.OnsTraceConstants;
import com.enode.rocketmq.trace.core.common.OnsTraceContext;
import com.enode.rocketmq.trace.core.common.OnsTraceType;
import com.enode.rocketmq.trace.core.dispatch.AsyncDispatcher;

import java.util.ArrayList;

public class OnsClientSendMessageHookImpl implements SendMessageHook {
    /**
     * 该Hook该由哪个dispatcher发送轨迹数据
     */
    private AsyncDispatcher localDispatcher;

    public OnsClientSendMessageHookImpl(AsyncDispatcher localDispatcher) {
        this.localDispatcher = localDispatcher;
    }

    @Override
    public String hookName() {
        return "OnsClientSendMessageHook";
    }

    @Override
    public void sendMessageBefore(SendMessageContext context) {
        // 如果是消息轨迹本身的发送链路，则不需要再记录
        if (context == null || context.getMessage().getTopic().startsWith(MixAll.SYSTEM_TOPIC_PREFIX)) {
            return;
        }
        OnsTraceContext onsContext = new OnsTraceContext();
        onsContext.setTraceBeans(new ArrayList<OnsTraceBean>(1));
        context.setMqTraceContext(onsContext);
        onsContext.setTraceType(OnsTraceType.Pub);
        onsContext.setGroupName(context.getProducerGroup());
        OnsTraceBean traceBean = new OnsTraceBean();
        traceBean.setTopic(context.getMessage().getTopic());
        traceBean.setTags(context.getMessage().getTags());
        traceBean.setKeys(context.getMessage().getKeys());
        traceBean.setStoreHost(context.getBrokerAddr());
        traceBean.setBodyLength(context.getMessage().getBody().length);
        traceBean.setMsgType(context.getMsgType());
        onsContext.getTraceBeans().add(traceBean);
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
        // 如果是消息轨迹本身的发送链路，则不需要再记录
        if (context == null || context.getMessage().getTopic().startsWith(OnsTraceConstants.traceTopic) || context.getMqTraceContext() == null) {
            return;
        }
        if (context.getSendResult() == null) {
            return;
        }
        if (context.getSendResult().getRegionId() == null
                || context.getSendResult().getRegionId().equals(OnsTraceConstants.default_region)
                /*|| !context.getSendResult().isTraceOn()*/) {
            // if regionId is default or switch is false,skip it
            return;
        }
        OnsTraceContext onsContext = (OnsTraceContext) context.getMqTraceContext();
        OnsTraceBean traceBean = onsContext.getTraceBeans().get(0);
        int costTime = (int) ((System.currentTimeMillis() - onsContext.getTimeStamp()) / onsContext.getTraceBeans().size());
        onsContext.setCostTime(costTime);
        if (context.getSendResult().getSendStatus().equals(SendStatus.SEND_OK)) {
            onsContext.setSuccess(true);
        } else {
            onsContext.setSuccess(false);
        }
        onsContext.setRegionId(context.getSendResult().getRegionId());
        traceBean.setMsgId(context.getSendResult().getMsgId());
        traceBean.setOffsetMsgId(context.getSendResult().getOffsetMsgId());
        traceBean.setStoreTime(onsContext.getTimeStamp() + costTime / 2);
        localDispatcher.append(onsContext);
    }
}
