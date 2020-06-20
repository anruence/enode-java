package org.enodeframework.spring;

import com.mongodb.reactivestreams.client.MongoClient;
import io.vertx.core.Vertx;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.eventing.impl.InMemoryEventStore;
import org.enodeframework.eventing.impl.InMemoryPublishedVersionStore;
import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.mongo.MongoEventStore;
import org.enodeframework.mongo.MongoPublishedVersionStore;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.enodeframework.pg.PgEventStore;
import org.enodeframework.pg.PgPublishedVersionStore;
import org.enodeframework.queue.DefaultSendReplyService;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
import org.enodeframework.tidb.TiDBEventStore;
import org.enodeframework.tidb.TiDBPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

public class EnodeEventStoreAutoConfig {

    private final Vertx vertx;

    public EnodeEventStoreAutoConfig() {
        vertx = Vertx.vertx();
    }

    @Bean
    public DefaultCommandResultProcessor commandResultProcessor() {
        DefaultCommandResultProcessor processor = new DefaultCommandResultProcessor();
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

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mongo")
    public MongoEventStore mongoEventStore(@Qualifier("enodeMongoClient") MongoClient mongoClient, IEventSerializer eventSerializer) {
        MongoEventStore eventStore = new MongoEventStore(mongoClient, eventSerializer);
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mongo")
    public MongoPublishedVersionStore mongoPublishedVersionStore(@Qualifier("enodeMongoClient") MongoClient mongoClient) {
        return new MongoPublishedVersionStore(mongoClient);
    }

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

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
    public PgEventStore pgEventStore(@Qualifier("enodePgDataSource") DataSource pgDataSource, IEventSerializer eventSerializer) {
        PgEventStore eventStore = new PgEventStore(pgDataSource, DBConfiguration.postgresql(), eventSerializer);
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

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "tidb")
    public TiDBEventStore tiDBEventStore(@Qualifier("enodeTiDBDataSource") DataSource tidbDataSource, IEventSerializer eventSerializer) {
        TiDBEventStore eventStore = new TiDBEventStore(tidbDataSource, DBConfiguration.mysql(), eventSerializer);
        vertx.deployVerticle(eventStore, res -> {
        });
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "tidb")
    public TiDBPublishedVersionStore tidbPublishedVersionStore(@Qualifier("enodeTiDBDataSource") DataSource tidbDataSource) {
        TiDBPublishedVersionStore publishedVersionStore = new TiDBPublishedVersionStore(tidbDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(publishedVersionStore, res -> {
        });
        return publishedVersionStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "memory")
    public InMemoryEventStore inMemoryEventStore() {
        return new InMemoryEventStore();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "memory")
    public InMemoryPublishedVersionStore inMemoryPublishedVersionStore() {
        return new InMemoryPublishedVersionStore();
    }

}
