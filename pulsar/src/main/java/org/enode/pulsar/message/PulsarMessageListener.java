package org.enode.pulsar.message;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;

/**
 * @author anruence@gmail.com
 */
public class PulsarMessageListener implements MessageListener<QueueMessage> {

    private final IMessageHandler messageHandler;

    public PulsarMessageListener(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void received(Consumer<QueueMessage> consumer, Message<QueueMessage> msg) {
        messageHandler.handle(msg.getValue(), x -> {
        });
    }
}