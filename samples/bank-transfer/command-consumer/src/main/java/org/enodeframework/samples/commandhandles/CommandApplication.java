package org.enodeframework.samples.commandhandles;

import org.enodeframework.spring.EnableEnode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author anruence@gmail.com
 */
@SpringBootApplication(scanBasePackages = "org.enodeframework")
@EnableEnode(basePackages = "org.enodeframework")
public class CommandApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommandApplication.class, args);
    }
}