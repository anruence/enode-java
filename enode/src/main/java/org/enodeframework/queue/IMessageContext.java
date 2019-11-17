package org.enodeframework.queue;

public interface IMessageContext {
    /**
     * 消息处理后执行
     */
    void onMessageHandled(QueueMessage message);
}
