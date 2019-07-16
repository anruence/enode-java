package org.enodeframework.spring;

import io.vertx.core.Vertx;
import org.enodeframework.common.scheduling.IScheduleService;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.queue.DefaultSendReplyService;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class EnodeVertxAutoConfig {

    private final static Logger logger = LoggerFactory.getLogger(EnodeVertxAutoConfig.class);

    @Autowired
    @Qualifier("enodeVertx")
    protected Vertx vertx;

    @Bean(value = "enodeVertx")
    public Vertx enodeVertx() {
        return Vertx.vertx();
    }

    @Value("${spring.enode.server.port:2019}")
    private int port;

    @Bean(name = "defaultCommandResultProcessor")
    @ConditionalOnProperty(prefix = "spring.enode", name = "server.port")
    public DefaultCommandResultProcessor defaultCommandResultProcessor(IScheduleService scheduleService) {
        DefaultCommandResultProcessor processor = new DefaultCommandResultProcessor(scheduleService, port);
        vertx.deployVerticle(processor, res -> {
            if (!res.succeeded()) {
                logger.error("vertx deploy DefaultCommandResultProcessor failed.", res.cause());
            }
        });
        return processor;
    }

    @Bean(name = "defaultSendReplyService")
    public DefaultSendReplyService defaultSendReplyService(ISerializeService serializeService) {
        DefaultSendReplyService sendReplyService = new DefaultSendReplyService(serializeService);
        vertx.deployVerticle(sendReplyService, res -> {
            if (!res.succeeded()) {
                logger.error("vertx deploy DefaultSendReplyService failed.", res.cause());
            }
        });
        return sendReplyService;
    }
}
