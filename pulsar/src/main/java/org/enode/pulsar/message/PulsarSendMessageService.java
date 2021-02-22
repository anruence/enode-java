package org.enode.pulsar.message;

import org.apache.pulsar.client.api.Producer;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class PulsarSendMessageService implements ISendMessageService {

    private static final Logger logger = LoggerFactory.getLogger(PulsarSendMessageService.class);

    private final Producer<QueueMessage> producer;

    public PulsarSendMessageService(Producer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Boolean> sendMessageAsync(QueueMessage queueMessage) {
        return producer.sendAsync(queueMessage)
                .exceptionally(throwable -> {
                    logger.error("Enode message async send has exception, message: {}, routingKey: {}", queueMessage.getBody(), queueMessage.getRouteKey(), throwable);
                    throw new IORuntimeException(throwable);
                })
                .thenApply(x -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Enode message async send success, sendResult: {}, message: {}", x.toByteArray(), queueMessage.getBody());
                    }
                    return true;
                });
    }
}
