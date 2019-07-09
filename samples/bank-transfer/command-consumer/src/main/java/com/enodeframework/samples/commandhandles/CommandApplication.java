package com.enodeframework.samples.commandhandles;

import com.enodeframework.ENodeAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author anruence@gmail.com
 */
@SpringBootApplication(scanBasePackages = "com.enodeframework")
@ImportAutoConfiguration(value = {ENodeAutoConfiguration.class, KafkaCommandConfig.class})
public class CommandApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommandApplication.class, args);
    }
}