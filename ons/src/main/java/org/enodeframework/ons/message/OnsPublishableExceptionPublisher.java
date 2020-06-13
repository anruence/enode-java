package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.publishableexceptions.AbstractPublishableExceptionPublisher;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class OnsPublishableExceptionPublisher extends AbstractPublishableExceptionPublisher {

    private Producer producer;

    @Override
    public CompletableFuture<Void> publishAsync(IDomainException exception) {
        QueueMessage queueMessage = createExceptionMessage(exception);
        Message message = OnsTool.covertToProducerRecord(queueMessage);
        return SendOnsService.sendMessageAsync(producer, message);
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }
}
