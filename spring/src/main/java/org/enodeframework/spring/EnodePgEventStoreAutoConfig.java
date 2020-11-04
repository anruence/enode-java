package org.enodeframework.spring;

import io.vertx.core.Vertx;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.pg.PgEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
public class EnodePgEventStoreAutoConfig {

    private static final Logger logger = LoggerFactory.getLogger(EnodePgEventStoreAutoConfig.class);
    @Autowired
    @Qualifier("enodeVertx")
    protected Vertx vertx;

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
    public PgEventStore pgEventStore(@Qualifier("enodePgDataSource") DataSource pgDataSource, IEventSerializer eventSerializer, ISerializeService serializeService) {
        PgEventStore eventStore = new PgEventStore(pgDataSource, DBConfiguration.postgresql(), eventSerializer, serializeService);
        vertx.deployVerticle(eventStore, res -> {
            if (!res.succeeded()) {
                logger.error("vertx deploy PgEventStore failed.", res.cause());
            }
        });
        return eventStore;
    }
}
