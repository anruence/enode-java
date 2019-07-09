package com.enodeframework.samples.eventhandlers;

import com.enodeframework.ENodeAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 事件接收服务
 */
@SpringBootApplication(scanBasePackages = {"com.enodeframework"})
@ImportAutoConfiguration(value = {ENodeAutoConfiguration.class, KafkaEventConfig.class})
public class EventApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventApplication.class, args);
    }
}