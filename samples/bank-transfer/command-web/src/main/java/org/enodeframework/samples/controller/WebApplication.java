package org.enodeframework.samples.controller;

import org.enodeframework.spring.EnableEnode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication(scanBasePackages = {"org.enodeframework"})
@EnableEnode(scanBasePackages = {"org.enodeframework"})
@EnableOpenApi
@EnableWebFlux
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}