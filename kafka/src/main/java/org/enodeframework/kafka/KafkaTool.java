package org.enodeframework.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.enodeframework.queue.QueueMessage;

/**
 * @author anruence@gmail.com
 */
public class KafkaTool {

    public static QueueMessage covertToQueueMessage(ConsumerRecord record) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(String.valueOf(record.value()));
        queueMessage.setTopic(record.topic());
        queueMessage.setRouteKey(String.valueOf(record.key()));
        return queueMessage;
    }

    public static ProducerRecord<String, String> covertToProducerRecord(QueueMessage queueMessage) {
        return new ProducerRecord<>(queueMessage.getTopic(), queueMessage.getRouteKey(), queueMessage.getBody());
    }
}
