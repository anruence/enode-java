package com.enodeframework.kafka;

import com.enodeframework.common.utilities.BitConverter;
import com.enodeframework.queue.QueueMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;

/**
 * @author anruence@gmail.com
 */
public class KafkaTool {
    public static String HEADERCODE = "CODE";

    public static QueueMessage covertToQueueMessage(ConsumerRecord record) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(String.valueOf(record.value()));
        queueMessage.setKey(String.valueOf(record.key()));
        queueMessage.setTopic(record.topic());
        return queueMessage;
    }

    public static ProducerRecord<String, String> covertToProducerRecord(QueueMessage queueMessage) {
        RecordHeaders headers = new RecordHeaders();
        RecordHeader header = new RecordHeader(HEADERCODE, BitConverter.getBytes(queueMessage.getCode()));
        headers.add(header);
        return new ProducerRecord<>(
                queueMessage.getTopic(),
                null,
                queueMessage.getRouteKey(),
                queueMessage.getBody(),
                headers
        );
    }
}
