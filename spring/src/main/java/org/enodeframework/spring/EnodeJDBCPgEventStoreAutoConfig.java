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

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc-pg")
public class EnodeJDBCPgEventStoreAutoConfig {

    @Bean
    public JDBCEventStore jdbcEventStore(@Qualifier("enodePgDataSource") DataSource enodePgDataSource, IEventSerializer eventSerializer, ISerializeService serializeService) {
        JDBCEventStore eventStore = new JDBCEventStore(enodePgDataSource, EventStoreConfiguration.pg(), eventSerializer, serializeService);
        return eventStore;
    }

    @Bean
    public JDBCPublishedVersionStore jdbcPublishedVersionStore(@Qualifier("enodePgDataSource") DataSource enodePgDataSource) {
        JDBCPublishedVersionStore publishedVersionStore = new JDBCPublishedVersionStore(enodePgDataSource, EventStoreConfiguration.pg());
        return publishedVersionStore;
    }
}
