package org.enodeframework.tests;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Vertx;
import org.enodeframework.ENodeBootstrap;
import org.enodeframework.commanding.impl.DefaultCommandProcessor;
import org.enodeframework.commanding.impl.DefaultProcessingCommandHandler;
import org.enodeframework.eventing.impl.DefaultEventCommittingService;
import org.enodeframework.eventing.impl.InMemoryEventStore;
import org.enodeframework.eventing.impl.InMemoryPublishedVersionStore;
import org.enodeframework.jdbc.DBConfiguration;
import org.enodeframework.mongo.MongoEventStore;
import org.enodeframework.mongo.MongoPublishedVersionStore;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.enodeframework.pg.PgEventStore;
import org.enodeframework.pg.PgPublishedVersionStore;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.enodeframework.tidb.TiDBEventStore;
import org.enodeframework.tidb.TiDBPublishedVersionStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(value = "org.enodeframework")
public class EnodeExtensionConfig {

    private final Vertx vertx = Vertx.vertx();

//    @PostConstruct
//    public void deployVerticle() {
//        vertx = Vertx.vertx();
//        vertx.deployVerticle(commandResultProcessor, res -> {
//        });
//        vertx.deployVerticle(eventStore, res -> {
//        });
//        vertx.deployVerticle(publishedVersionStore, res -> {
//        });
//    }

    @Bean
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor();
        vertx.deployVerticle(processor, res -> {
        });
        return processor;
    }

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap("org.enodeframework.tests");
        return bootstrap;
    }

    @Bean
    public DefaultProcessingCommandHandler defaultProcessingCommandHandler() {
        return new DefaultProcessingCommandHandler();
    }

    @Bean
    public DefaultEventCommittingService defaultEventService() {
        return new DefaultEventCommittingService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DefaultCommandProcessor defaultCommandProcessor() {
        return new DefaultCommandProcessor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "mongo")
    public MongoEventStore mongoEventStore(MongoClient mongoClient) {
        MongoEventStore eventStore = new MongoEventStore(mongoClient);
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "mongo")
    public MongoPublishedVersionStore mongoPublishedVersionStore(MongoClient mongoClient) {
        return new MongoPublishedVersionStore(mongoClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "mongo")
    public MongoClient mongoClient() {
        return MongoClients.create();
    }


    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "mysql")
    public MysqlEventStore mysqlEventStore(HikariDataSource mysqlDataSource) {
        MysqlEventStore eventStore = new MysqlEventStore(mysqlDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(eventStore, res -> {
        });
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "mysql")
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(HikariDataSource mysqlDataSource) {
        MysqlPublishedVersionStore publishedVersionStore = new MysqlPublishedVersionStore(mysqlDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(publishedVersionStore, res -> {
        });
        return publishedVersionStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "pg")
    public PgEventStore pgEventStore(HikariDataSource pgDataSource) {
        PgEventStore eventStore = new PgEventStore(pgDataSource, DBConfiguration.postgresql());
        vertx.deployVerticle(eventStore, res -> {
        });
        return eventStore;
    }

    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "pg")
    @Bean
    public PgPublishedVersionStore pgPublishedVersionStore(HikariDataSource pgDataSource) {
        PgPublishedVersionStore versionStore = new PgPublishedVersionStore(pgDataSource, DBConfiguration.postgresql());
        vertx.deployVerticle(versionStore, res -> {
        });
        return versionStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "tidb")
    public TiDBEventStore tiDBEventStore(HikariDataSource tidbDataSource) {
        TiDBEventStore eventStore = new TiDBEventStore(tidbDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(eventStore, res -> {
        });
        return eventStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "tidb")
    public TiDBPublishedVersionStore tidbPublishedVersionStore(HikariDataSource tidbDataSource) {
        TiDBPublishedVersionStore publishedVersionStore = new TiDBPublishedVersionStore(tidbDataSource, DBConfiguration.mysql());
        vertx.deployVerticle(publishedVersionStore, res -> {
        });
        return publishedVersionStore;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "tidb")
    public HikariDataSource tidbDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:4000/enode?");
        dataSource.setUsername("root");
        dataSource.setPassword("");
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "mysql")
    public HikariDataSource mysqlDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/enode?");
        dataSource.setUsername("root");
        dataSource.setPassword("abcd1234&ABCD");
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "pg")
    public HikariDataSource pgDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/enode");
        dataSource.setUsername("postgres");
        dataSource.setPassword("mysecretpassword");
        dataSource.setDriverClassName(org.postgresql.Driver.class.getName());
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "memory")
    public InMemoryEventStore inMemoryEventStore() {
        return new InMemoryEventStore();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.enode.eventstore", name = "memory")
    public InMemoryPublishedVersionStore inMemoryPublishedVersionStore() {
        return new InMemoryPublishedVersionStore();
    }

}
