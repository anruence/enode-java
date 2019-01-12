package com.qianzhui.enode.rocketmq.trace.core.utils;

import com.alibaba.rocketmq.client.hook.ConsumeMessageContext;
import com.alibaba.rocketmq.client.hook.ConsumeMessageHook;
import com.alibaba.rocketmq.common.message.MessageConst;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.qianzhui.enode.rocketmq.trace.core.common.OnsTraceBean;
import com.qianzhui.enode.rocketmq.trace.core.common.OnsTraceConstants;
import com.qianzhui.enode.rocketmq.trace.core.common.OnsTraceContext;
import com.qianzhui.enode.rocketmq.trace.core.common.OnsTraceType;
import com.qianzhui.enode.rocketmq.trace.core.dispatch.AsyncDispatcher;

import java.util.ArrayList;
import java.util.List;

public class OnsConsumeMessageHookImpl implements ConsumeMessageHook {
    /**
     * 该Hook该由哪个dispatcher发送轨迹数据
     */
    private AsyncDispatcher localDispatcher;

    public OnsConsumeMessageHookImpl(AsyncDispatcher localDispatcher) {
        this.localDispatcher = localDispatcher;
    }

    @Override
    public String hookName() {
        return "OnsConsumeMessageHook";
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        if (context == null || context.getMsgList() == null || context.getMsgList().isEmpty()) {
            return;
        }
        OnsTraceContext onsTraceContext = new OnsTraceContext();
        context.setMqTraceContext(onsTraceContext);
        onsTraceContext.setTraceType(OnsTraceType.SubBefore);//
        onsTraceContext.setGroupName(context.getConsumerGroup());//
        List<OnsTraceBean> beans = new ArrayList<OnsTraceBean>();
        for (MessageExt msg : context.getMsgList()) {
            if (msg == null) {
                continue;
            }
            String regionId = msg.getProperty(MessageConst.PROPERTY_MSG_REGION);
            String traceOn = msg.getProperty("TRACE_ON");
            if (regionId == null || regionId.equals(OnsTraceConstants.default_region)) {
                // if regionId is default ,skip it
                continue;
            }
            if (traceOn != null && "false".equals(traceOn)) {
                // if trace switch is false ,skip it
                continue;
            }
            OnsTraceBean traceBean = new OnsTraceBean();
            traceBean.setTopic(msg.getTopic());//
            traceBean.setMsgId(msg.getMsgId());//
            traceBean.setTags(msg.getTags());//
            traceBean.setKeys(msg.getKeys());//
            traceBean.setStoreTime(msg.getStoreTimestamp());//
            traceBean.setBodyLength(msg.getStoreSize());//
            traceBean.setRetryTimes(msg.getReconsumeTimes());//
            onsTraceContext.setRegionId(regionId);//
            beans.add(traceBean);
        }
        if (beans.size() > 0) {
            onsTraceContext.setTraceBeans(beans);
            onsTraceContext.setTimeStamp(System.currentTimeMillis());
            localDispatcher.append(onsTraceContext);
        }
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
        if (context == null || context.getMsgList() == null || context.getMsgList().isEmpty()) {
            return;
        }
        OnsTraceContext subBeforeContext = (OnsTraceContext) context.getMqTraceContext();
        if (subBeforeContext.getRegionId().equals(OnsTraceConstants.default_region)) {
            // if regionId is default ,skip it
            return;
        }
        if (subBeforeContext.getTraceBeans() == null || subBeforeContext.getTraceBeans().size() < 1) {
            // if subbefore bean is null ,skip it
            return;
        }
        OnsTraceContext subAfterContext = new OnsTraceContext();
        subAfterContext.setTraceType(OnsTraceType.SubAfter);//
        subAfterContext.setRegionId(subBeforeContext.getRegionId());//
        subAfterContext.setGroupName(subBeforeContext.getGroupName());//
        subAfterContext.setRequestId(subBeforeContext.getRequestId());//
        subAfterContext.setSuccess(context.isSuccess());//
        // 批量消息全部处理完毕的平均耗时
        int costTime = (int) ((System.currentTimeMillis() - subBeforeContext.getTimeStamp()) / context.getMsgList().size());
        subAfterContext.setCostTime(costTime);//
        subAfterContext.setTraceBeans(subBeforeContext.getTraceBeans());
        String contextType = context.getProps().get("ConsumeContextType");
        if (contextType != null) {
            subAfterContext.setContextCode(ConsumeReturnType.valueOf(contextType).ordinal());
        }
        localDispatcher.append(subAfterContext);
    }

    public enum ConsumeReturnType {
        /**
         * consume return success
         */
        SUCCESS,
        /**
         * consume timeout ,even if success
         */
        TIME_OUT,
        /**
         * consume throw exception
         */
        EXCEPTION,
        /**
         * consume return null
         */
        RETURNNULL,
        /**
         * consume return failed
         */
        FAILED
    }
}