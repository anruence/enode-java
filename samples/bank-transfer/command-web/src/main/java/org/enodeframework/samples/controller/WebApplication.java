package org.enodeframework.samples.controller;

import io.vertx.core.Vertx;
import org.enodeframework.queue.DefaultSendReplyService;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
import org.enodeframework.spring.EnableEnode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

    @Autowired
    private DefaultCommandResultProcessor commandResultProcessor;

    @Autowired
    private DefaultSendReplyService sendReplyService;

    @Bean
    public Vertx vertx() {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(commandResultProcessor);
        vertx.deployVerticle(sendReplyService);
        return vertx;
    }
}