package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import org.enodeframework.queue.IMessageHandler;

import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class OnsBatchMessageListener implements BatchMessageListener {

    private final IMessageHandler messageHandler;

    public OnsBatchMessageListener(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public Action consume(List<Message> messages, ConsumeContext context) {
        OnsTool.handle(messages, messageHandler);
        return Action.CommitMessage;
    }
}