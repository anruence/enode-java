package com.enode.kafka;

import com.enode.common.utilities.BitConverter;
import com.enode.queue.QueueMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;

public class KafkaTool {

    public static String HEADER_CODE = "CODE";

    public static QueueMessage covertToQueueMessage(ConsumerRecord record) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(record.value().toString());
        queueMessage.setKey(record.key().toString());
        queueMessage.setTopic(record.topic());
        return queueMessage;
    }

    public static ProducerRecord<String, String> covertToProducerRecord(QueueMessage queueMessage) {
        RecordHeaders headers = new RecordHeaders();
        RecordHeader header = new RecordHeader(HEADER_CODE, BitConverter.getBytes(queueMessage.getCode()));
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
