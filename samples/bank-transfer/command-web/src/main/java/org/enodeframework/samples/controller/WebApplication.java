package org.enodeframework.samples.controller;

import org.enodeframework.spring.EnodeAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.enodeframework"})
@ImportAutoConfiguration(value = {EnodeAutoConfiguration.class, KafkaConfig.class})
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}