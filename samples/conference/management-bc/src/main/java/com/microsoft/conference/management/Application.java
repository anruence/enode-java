package com.microsoft.conference.management;

import com.microsoft.conference.common.config.ConferenceDataSourceConfiguration;
import com.microsoft.conference.common.config.KafkaEventConfig;
import org.enodeframework.spring.EnableEnode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.microsoft"})
@EnableEnode(scanBasePackages = {"com.microsoft"})
@ImportAutoConfiguration(value = {ConferenceDataSourceConfiguration.class, KafkaEventConfig.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}