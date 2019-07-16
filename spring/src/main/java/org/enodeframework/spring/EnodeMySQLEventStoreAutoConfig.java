package org.enodeframework.spring;

import io.vertx.core.Vertx;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
public class EnodeMySQLEventStoreAutoConfig {

    @Autowired
    @Qualifier("enodeVertx")
    private Vertx vertx;

    private final static Logger logger = LoggerFactory.getLogger(EnodeMySQLEventStoreAutoConfig.class);

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
    public MysqlEventStore mysqlEventStore(@Qualifier("enodeMySQLDataSource") DataSource dataSource, IEventSerializer eventSerializer, ISerializeService serializeService) {
        MysqlEventStore eventStore = new MysqlEventStore(dataSource, DBConfiguration.mysql(), eventSerializer, serializeService);
        vertx.deployVerticle(eventStore, res -> {
            if (!res.succeeded()) {
                logger.error("vertx deploy MysqlEventStore failed.", res.cause());
            }
        });
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(@Qualifier("enodeMySQLDataSource") DataSource mySQLDataSource) {
        MysqlPublishedVersionStore publishedVersionStore = new MysqlPublishedVersionStore(mySQLDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(publishedVersionStore, res -> {
            if (!res.succeeded()) {
                logger.error("vertx deploy MysqlPublishedVersionStore failed.", res.cause());
            }
        });
        return publishedVersionStore;
    }
}
