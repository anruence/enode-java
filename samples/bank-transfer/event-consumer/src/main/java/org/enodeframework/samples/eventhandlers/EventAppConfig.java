package org.enodeframework.samples.eventhandlers;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Vertx;
import org.enodeframework.ENodeBootstrap;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static org.enodeframework.samples.QueueProperties.JDBC_URL;

@Configuration
public class EventAppConfig {
    private Vertx vertx;
    @Autowired
    private MysqlEventStore mysqlEventStore;

//    @Bean
//    public InMemoryEventStore inMemoryEventStore() {
//        return new InMemoryEventStore();
//    }
//
//    @Bean
//    public InMemoryPublishedVersionStore inMemoryPublishedVersionStore() {
//        return new InMemoryPublishedVersionStore();
//    }
    @Autowired
    private MysqlPublishedVersionStore publishedVersionStore;
    @Autowired
    private CommandResultProcessor commandResultProcessor;

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("org.enodeframework.samples"));
        return bootstrap;
    }

    @Bean
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor(6001);
        return processor;
    }

    @Bean
    public MysqlEventStore mysqlEventStore(HikariDataSource dataSource) {
        MysqlEventStore mysqlEventStore = new MysqlEventStore(dataSource, null);
        return mysqlEventStore;
    }

    @Bean
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(HikariDataSource dataSource) {
        MysqlPublishedVersionStore p = new MysqlPublishedVersionStore(dataSource, null);
        return p;
    }

    @Bean
    public HikariDataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(JDBC_URL);
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @PostConstruct
    public void deployVerticle() {
        vertx = Vertx.vertx();
        vertx.deployVerticle(commandResultProcessor, res -> {

        });
        vertx.deployVerticle(mysqlEventStore, res -> {

        });
        vertx.deployVerticle(publishedVersionStore, res -> {

        });
    }


}
