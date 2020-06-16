package org.enodeframework.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.enodeframework.queue.QueueMessage;

/**
 * @author anruence@gmail.com
 */
public class KafkaTool {

    public static QueueMessage covertToQueueMessage(ConsumerRecord<String, String> record) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(record.value());
        queueMessage.setTopic(record.topic());
        queueMessage.setRouteKey(record.key());
        return queueMessage;
    }

    public static ProducerRecord<String, String> covertToProducerRecord(QueueMessage queueMessage) {
        return new ProducerRecord<>(queueMessage.getTopic(), queueMessage.getRouteKey(), queueMessage.getBody());
    }
}
