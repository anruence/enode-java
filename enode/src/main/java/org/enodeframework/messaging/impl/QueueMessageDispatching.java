package org.enodeframework.messaging.impl;

import org.enodeframework.messaging.IMessage;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueMessageDispatching {
    private final DefaultMessageDispatcher dispatcher;
    private final RootDispatching rootDispatching;
    private final ConcurrentLinkedQueue<IMessage> messageQueue;

    public QueueMessageDispatching(DefaultMessageDispatcher dispatcher, RootDispatching rootDispatching, List<? extends IMessage> messages) {
        this.dispatcher = dispatcher;
        messageQueue = new ConcurrentLinkedQueue<>();
        messageQueue.addAll(messages);
        this.rootDispatching = rootDispatching;
        this.rootDispatching.addChildDispatching(this);
    }

    public IMessage dequeueMessage() {
        return messageQueue.poll();
    }

    public void onMessageHandled(IMessage message) {
        IMessage nextMessage = dequeueMessage();
        if (nextMessage == null) {
            rootDispatching.onChildDispatchingFinished(this);
            return;
        }
        dispatcher.dispatchSingleMessage(nextMessage, this);
    }
}
