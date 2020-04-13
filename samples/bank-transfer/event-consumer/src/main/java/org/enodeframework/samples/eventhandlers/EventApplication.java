package org.enodeframework.samples.eventhandlers;

import io.vertx.core.Vertx;
import org.enodeframework.ENodeAutoConfiguration;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * 事件接收服务
 */
@SpringBootApplication(scanBasePackages = {"org.enodeframework"})
@ImportAutoConfiguration(value = {ENodeAutoConfiguration.class, KafkaEventConfig.class})
public class EventApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventApplication.class, args);
    }


}