package org.enodeframework.tests;

import com.google.common.collect.Lists;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Vertx;
import org.enodeframework.ENodeBootstrap;
import org.enodeframework.commanding.impl.DefaultCommandProcessor;
import org.enodeframework.commanding.impl.DefaultProcessingCommandHandler;
import org.enodeframework.eventing.impl.DefaultEventCommittingService;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.enodeframework.pg.MongoEventStore;
import org.enodeframework.pg.MongoPublishedVersionStore;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

@ComponentScan(value = "org.enodeframework")
public class EnodeExtensionConfig {

    private Vertx vertx;

    @Autowired
    private CommandResultProcessor commandResultProcessor;

//    @Autowired
//    private MysqlEventStore eventStore;
//
//    @Autowired
//    private MysqlPublishedVersionStore publishedVersionStore;

    @PostConstruct
    public void deployVerticle() {
        vertx = Vertx.vertx();
        vertx.deployVerticle(commandResultProcessor, res -> {
        });
//        vertx.deployVerticle(eventStore, res -> {
//        });
//        vertx.deployVerticle(publishedVersionStore, res -> {
//        });
    }

    @Bean
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor();
        return processor;
    }

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setScanPackages(Lists.newArrayList("org.enodeframework.tests"));
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
    public MongoEventStore mongoEventStore() {
        MongoClient mongoClient = MongoClients.create();
        MongoEventStore mongoEventStore = new MongoEventStore(mongoClient);
        return mongoEventStore;
    }

    @Bean
    public MongoPublishedVersionStore mongoPublishedVersionStore() {
        MongoClient mongoClient = MongoClients.create();
        return new MongoPublishedVersionStore(mongoClient);
    }

//    @Bean
//    public MysqlEventStore mysqlEventStore(HikariDataSource dataSource) {
//        MysqlEventStore mysqlEventStore = new MysqlEventStore(dataSource);
//        return mysqlEventStore;
//    }
//
//    @Bean
//    public MysqlPublishedVersionStore mysqlPublishedVersionStore(HikariDataSource dataSource) {
//        return new MysqlPublishedVersionStore(dataSource);
//    }

//    @Bean
//    public InMemoryEventStore inMemoryEventStore() {
//        return new InMemoryEventStore();
//    }
//
//    @Bean
//    public InMemoryPublishedVersionStore inMemoryPublishedVersionStore() {
//        return new InMemoryPublishedVersionStore();
//    }

    @Bean
    public HikariDataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/enode?");
        dataSource.setUsername("root");
        dataSource.setPassword("abcd1234&ABCD");
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }
}
