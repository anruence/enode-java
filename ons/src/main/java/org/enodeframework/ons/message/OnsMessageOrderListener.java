package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.google.common.collect.Lists;
import org.enodeframework.queue.IMessageHandler;

/**
 * @author anruence@gmail.com
 */
public class OnsMessageOrderListener implements MessageOrderListener {

    private final IMessageHandler messageHandler;

    public OnsMessageOrderListener(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public OrderAction consume(Message message, ConsumeOrderContext consumeOrderContext) {
        OnsTool.handle(Lists.newArrayList(message), messageHandler);
        return OrderAction.Success;
    }
}