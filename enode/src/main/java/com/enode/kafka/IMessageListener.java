package com.enode.kafka;

import com.enode.rocketmq.CompletableConsumeConcurrentlyContext;
import com.enode.rocketmq.command.ConsumeStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface IMessageListener {

    ConsumeStatus recvMessage(ConsumerRecord message, CompletableConsumeConcurrentlyContext context);

}
