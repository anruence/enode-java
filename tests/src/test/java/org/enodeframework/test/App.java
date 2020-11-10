package org.enodeframework.test;

import org.enodeframework.spring.EnableEnode;
import org.enodeframework.test.config.EnodeTestDataSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ContextConfiguration;

@SpringBootApplication
@EnableEnode
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
