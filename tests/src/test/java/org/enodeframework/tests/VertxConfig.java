package org.enodeframework.tests;

import io.vertx.core.Vertx;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class VertxConfig {

    private Vertx vertx;

    @Autowired
    private CommandResultProcessor commandResultProcessor;

    @PostConstruct
    public void deployVerticle() {
        vertx = Vertx.vertx();
        vertx.deployVerticle(commandResultProcessor, res -> {
        });
    }

}
