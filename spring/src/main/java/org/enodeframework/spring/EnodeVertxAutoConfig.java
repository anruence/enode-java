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
            if (!res.succeeded()) {
                logger.error("vertx deploy DefaultCommandResultProcessor failed.", res.cause());
            }
        });
        return processor;
    }

    @Bean
    public DefaultSendReplyService sendReplyService(ISerializeService serializeService) {
        DefaultSendReplyService sendReplyService = new DefaultSendReplyService(serializeService);
        vertx.deployVerticle(sendReplyService, res -> {
            if (!res.succeeded()) {
                logger.error("vertx deploy DefaultSendReplyService failed.", res.cause());
            }
        });
        return sendReplyService;
    }
}
