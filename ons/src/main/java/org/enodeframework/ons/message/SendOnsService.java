package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.ISendMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class SendOnsService implements ISendMessageService {

    private static final Logger logger = LoggerFactory.getLogger(SendOnsService.class);

    private final Producer producer;

    public SendOnsService(Producer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Void> sendMessageAsync(QueueMessage queueMessage) {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        Message message = OnsTool.covertToProducerRecord(queueMessage);
        producer.sendAsync(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Enode message async send success, sendResult: {}, message: {}", result, message);
                }
                promise.complete(null);
            }

            @Override
            public void onException(OnExceptionContext onExceptionContext) {
                promise.completeExceptionally(new IORuntimeException(onExceptionContext.getException()));
                logger.error("Enode message async send has exception, message: {}, routingKey: {}", message, message.getShardingKey(), onExceptionContext.getException());
            }
        });
        return promise;
    }
}
