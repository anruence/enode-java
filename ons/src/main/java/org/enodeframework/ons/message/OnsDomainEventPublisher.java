package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.domainevent.AbstractDomainEventPublisher;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class OnsDomainEventPublisher extends AbstractDomainEventPublisher {

    private Producer producer;

    @Override
    public CompletableFuture<Void> publishAsync(DomainEventStreamMessage eventStream) {
        QueueMessage queueMessage = createDomainEventStreamMessage(eventStream);
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
