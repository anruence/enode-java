package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.google.common.collect.Lists;
import org.enodeframework.queue.IMessageHandler;

import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class OnsMessageListener implements BatchMessageListener, MessageListener, MessageOrderListener {

    private final IMessageHandler messageHandler;

    public OnsMessageListener(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public Action consume(List<Message> messages, ConsumeContext context) {
        OnsTool.handle(messages, messageHandler);
        return Action.CommitMessage;
    }

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        OnsTool.handle(Lists.newArrayList(message), messageHandler);
        return Action.CommitMessage;
    }

    @Override
    public OrderAction consume(Message message, ConsumeOrderContext consumeOrderContext) {
        OnsTool.handle(Lists.newArrayList(message), messageHandler);
        return OrderAction.Success;
    }
}