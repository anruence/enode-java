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
public class OnsApplicationMessageListener implements MessageOrderListener {

    private final IMessageHandler applicationMessageListener;

    public OnsApplicationMessageListener(IMessageHandler applicationMessageListener) {
        this.applicationMessageListener = applicationMessageListener;
    }

    @Override
    public OrderAction consume(Message message, ConsumeOrderContext context) {
        OnsTool.handle(Lists.newArrayList(message), applicationMessageListener);
        return OrderAction.Success;
    }


    public Action consume(List<Message> messages, ConsumeContext context) {
        OnsTool.handle(messages, applicationMessageListener);
        return Action.CommitMessage;
    }
}