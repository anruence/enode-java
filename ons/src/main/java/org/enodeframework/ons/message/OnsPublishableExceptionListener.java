package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.google.common.collect.Lists;
import org.enodeframework.queue.IMessageHandler;

import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class OnsPublishableExceptionListener implements MessageOrderListener {

    private final IMessageHandler publishableExceptionListener;

    public OnsPublishableExceptionListener(IMessageHandler publishableExceptionListener) {
        this.publishableExceptionListener = publishableExceptionListener;
    }

    @Override
    public OrderAction consume(Message message, ConsumeOrderContext context) {
        OnsTool.handle(Lists.newArrayList(message), publishableExceptionListener);
        return OrderAction.Success;
    }


    public Action consume(List<Message> messages, ConsumeContext context) {
        OnsTool.handle(messages, publishableExceptionListener);
        return Action.CommitMessage;
    }
}
