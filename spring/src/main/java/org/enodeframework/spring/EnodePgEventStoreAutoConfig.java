package org.enodeframework.spring;

import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.pg.PgEventStore;
import org.enodeframework.pg.PgPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
public class EnodePgEventStoreAutoConfig extends EnodeVertxAutoConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
    public PgEventStore pgEventStore(@Qualifier("enodePgDataSource") DataSource pgDataSource, IEventSerializer eventSerializer, ISerializeService serializeService) {
        PgEventStore eventStore = new PgEventStore(pgDataSource, DBConfiguration.postgresql(), eventSerializer, serializeService);
        vertx.deployVerticle(eventStore, res -> {
        });
        return eventStore;
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
    @Bean
    public PgPublishedVersionStore pgPublishedVersionStore(@Qualifier("enodePgDataSource") DataSource pgDataSource) {
        PgPublishedVersionStore versionStore = new PgPublishedVersionStore(pgDataSource, DBConfiguration.postgresql());
        vertx.deployVerticle(versionStore, res -> {
        });
        return versionStore;
    }
}
