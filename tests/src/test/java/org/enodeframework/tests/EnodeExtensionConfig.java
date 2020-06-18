package org.enodeframework.tests;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.zaxxer.hikari.HikariDataSource;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class EnodeExtensionConfig {

    private final Vertx vertx = Vertx.vertx();

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
    public MongoEventStore mongoEventStore(IEventSerializer eventSerializer, MongoClient mongoClient) {
        MongoEventStore eventStore = new MongoEventStore(mongoClient, eventSerializer);
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mongo")
    public MongoPublishedVersionStore mongoPublishedVersionStore(MongoClient mongoClient) {
        return new MongoPublishedVersionStore(mongoClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mongo")
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
    public MysqlEventStore mysqlEventStore(IEventSerializer eventSerializer, HikariDataSource mysqlDataSource) {
        MysqlEventStore eventStore = new MysqlEventStore(mysqlDataSource, DBConfiguration.mysql(), eventSerializer);
        vertx.deployVerticle(eventStore, res -> {
        });
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(HikariDataSource mysqlDataSource) {
        MysqlPublishedVersionStore publishedVersionStore = new MysqlPublishedVersionStore(mysqlDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(publishedVersionStore, res -> {
        });
        return publishedVersionStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
    public PgEventStore pgEventStore(IEventSerializer eventSerializer, HikariDataSource pgDataSource) {
        PgEventStore eventStore = new PgEventStore(pgDataSource, DBConfiguration.postgresql(), eventSerializer);
        vertx.deployVerticle(eventStore, res -> {
        });
        return eventStore;
    }

    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
    @Bean
    public PgPublishedVersionStore pgPublishedVersionStore(HikariDataSource pgDataSource) {
        PgPublishedVersionStore versionStore = new PgPublishedVersionStore(pgDataSource, DBConfiguration.postgresql());
        vertx.deployVerticle(versionStore, res -> {
        });
        return versionStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "tidb")
    public TiDBEventStore tiDBEventStore(IEventSerializer eventSerializer, HikariDataSource tidbDataSource) {
        TiDBEventStore eventStore = new TiDBEventStore(tidbDataSource, DBConfiguration.mysql(), eventSerializer);
        vertx.deployVerticle(eventStore, res -> {
        });
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "tidb")
    public TiDBPublishedVersionStore tidbPublishedVersionStore(HikariDataSource tidbDataSource) {
        TiDBPublishedVersionStore publishedVersionStore = new TiDBPublishedVersionStore(tidbDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(publishedVersionStore, res -> {
        });
        return publishedVersionStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "tidb")
    public HikariDataSource tidbDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:4000/enode?");
        dataSource.setUsername("root");
        dataSource.setPassword("");
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mysql")
    public HikariDataSource mysqlDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/enode?");
        dataSource.setUsername("root");
        dataSource.setPassword("abcd1234&ABCD");
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "pg")
    public HikariDataSource pgDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/enode");
        dataSource.setUsername("postgres");
        dataSource.setPassword("mysecretpassword");
        dataSource.setDriverClassName(org.postgresql.Driver.class.getName());
        return dataSource;
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
