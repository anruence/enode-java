package org.enodeframework.spring;

import io.vertx.core.Vertx;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.tidb.TiDBEventStore;
import org.enodeframework.tidb.TiDBPublishedVersionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "tidb")
public class EnodeTiDBEventStoreAutoConfig {

    private final static Logger logger = LoggerFactory.getLogger(EnodeTiDBEventStoreAutoConfig.class);

    @Autowired
    @Qualifier("enodeVertx")
    protected Vertx vertx;

    @Bean
    public TiDBEventStore tiDBEventStore(@Qualifier("enodeTiDBDataSource") DataSource tidbDataSource, IEventSerializer eventSerializer, ISerializeService serializeService) {
        TiDBEventStore eventStore = new TiDBEventStore(tidbDataSource, DBConfiguration.mysql(), eventSerializer, serializeService);
        vertx.deployVerticle(eventStore, res -> {
            if (!res.succeeded()) {
                logger.error("vertx deploy TiDBEventStore failed.", res.cause());
            }
        });
        return eventStore;
    }

    @Bean
    public TiDBPublishedVersionStore tidbPublishedVersionStore(@Qualifier("enodeTiDBDataSource") DataSource tidbDataSource) {
        TiDBPublishedVersionStore publishedVersionStore = new TiDBPublishedVersionStore(tidbDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(publishedVersionStore, res -> {
            if (!res.succeeded()) {
                logger.error("vertx deploy TiDBPublishedVersionStore failed.", res.cause());
            }
        });
        return publishedVersionStore;
    }
}
