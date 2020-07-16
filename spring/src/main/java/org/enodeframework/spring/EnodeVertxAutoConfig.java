package org.enodeframework.spring;

import io.vertx.core.Vertx;
import org.enodeframework.common.scheduling.IScheduleService;
import org.enodeframework.queue.DefaultSendReplyService;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class EnodeVertxAutoConfig {

    @Autowired
    @Qualifier("enodeVertx")
    protected Vertx vertx;
    @Value("${spring.enode.server.port:2019}")
    private int port;

    @Bean(value = "enodeVertx")
    public Vertx enodeVertx() {
        return Vertx.vertx();
    }

    @Bean(name = "defaultCommandResultProcessor")
    @ConditionalOnProperty(prefix = "spring.enode", name = "server.port")
    public DefaultCommandResultProcessor commandResultProcessor(IScheduleService scheduleService) {
        DefaultCommandResultProcessor processor = new DefaultCommandResultProcessor(scheduleService, port);
        vertx.deployVerticle(processor, res -> {
        });
        return processor;
    }

    @Bean
    public DefaultSendReplyService sendReplyService() {
        DefaultSendReplyService sendReplyService = new DefaultSendReplyService();
        vertx.deployVerticle(sendReplyService, res -> {
        });
        return sendReplyService;
    }
}
