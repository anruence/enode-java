package org.enodeframework.spring;

import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
public class EnodeMysqlEventStoreAutoConfig extends EnodeVertxAutoConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
    public MysqlEventStore mysqlEventStore(@Qualifier("enodeMysqlDataSource") DataSource mysqlDataSource, IEventSerializer eventSerializer) {
        MysqlEventStore eventStore = new MysqlEventStore(mysqlDataSource, DBConfiguration.mysql(), eventSerializer);
        vertx.deployVerticle(eventStore, res -> {
        });
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(@Qualifier("enodeMysqlDataSource") DataSource mysqlDataSource) {
        MysqlPublishedVersionStore publishedVersionStore = new MysqlPublishedVersionStore(mysqlDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(publishedVersionStore, res -> {
        });
        return publishedVersionStore;
    }
}
