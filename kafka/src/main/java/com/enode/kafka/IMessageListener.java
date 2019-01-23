package com.enode.kafka;

import com.enode.queue.CompletableConsumeConcurrentlyContext;
import com.enode.queue.command.ConsumeStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface IMessageListener {

    ConsumeStatus receiveMessage(ConsumerRecord message, CompletableConsumeConcurrentlyContext context);

}
