package org.enodeframework.samples.controller;

import org.enodeframework.ENodeAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.enodeframework"})
@ImportAutoConfiguration(value = {ENodeAutoConfiguration.class, KafkaConfig.class})
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}