package org.enodeframework.samples.commandhandles;

import org.enodeframework.spring.EnodeAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author anruence@gmail.com
 */
@SpringBootApplication(scanBasePackages = "org.enodeframework")
@ImportAutoConfiguration(value = {EnodeAutoConfiguration.class, KafkaCommandConfig.class})
public class CommandApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommandApplication.class, args);
    }
}