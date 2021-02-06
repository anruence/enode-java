package org.enodeframework.spring;

import org.enodeframework.common.EventStoreConfiguration;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.jdbc.JDBCEventStore;
import org.enodeframework.jdbc.JDBCPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc-mysql")
public class EnodeJDBCMySQLEventStoreAutoConfig {

    @Bean
    public JDBCEventStore jdbcEventStore(@Qualifier("enodeMySQLDataSource") DataSource enodeMySQLDataSource, IEventSerializer eventSerializer, ISerializeService serializeService) {
        JDBCEventStore eventStore = new JDBCEventStore(enodeMySQLDataSource, EventStoreConfiguration.mysql(), eventSerializer, serializeService);
        return eventStore;
    }

    @Bean
    public JDBCPublishedVersionStore jdbcPublishedVersionStore(@Qualifier("enodeMySQLDataSource") DataSource enodeMySQLDataSource) {
        JDBCPublishedVersionStore publishedVersionStore = new JDBCPublishedVersionStore(enodeMySQLDataSource, EventStoreConfiguration.mysql());
        return publishedVersionStore;
    }
}
