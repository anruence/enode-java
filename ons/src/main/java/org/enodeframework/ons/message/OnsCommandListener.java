package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
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
public class OnsCommandListener implements MessageOrderListener, BatchMessageListener {

    private final IMessageHandler commandListener;

    public OnsCommandListener(IMessageHandler commandListener) {
        this.commandListener = commandListener;
    }

    @Override
    public OrderAction consume(Message message, ConsumeOrderContext context) {
        OnsTool.handle(Lists.newArrayList(message), commandListener);
        return OrderAction.Success;
    }

    @Override
    public Action consume(List<Message> messages, ConsumeContext context) {
        OnsTool.handle(messages, commandListener);
        return Action.CommitMessage;
    }
}
