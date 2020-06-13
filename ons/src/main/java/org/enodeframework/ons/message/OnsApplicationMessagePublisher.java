package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.applicationmessage.AbstractApplicationMessagePublisher;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class OnsApplicationMessagePublisher extends AbstractApplicationMessagePublisher {

    private Producer producer;

    @Override
    public CompletableFuture<Void> publishAsync(IApplicationMessage message) {
        QueueMessage queueMessage = createApplicationMessage(message);
        Message msg = OnsTool.covertToProducerRecord(queueMessage);
        return SendOnsService.sendMessageAsync(producer, msg);
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }
}
