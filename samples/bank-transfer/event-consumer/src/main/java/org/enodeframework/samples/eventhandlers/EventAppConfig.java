package org.enodeframework.samples.eventhandlers;

import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Vertx;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
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

    @Autowired
    private MysqlPublishedVersionStore publishedVersionStore;

    @Autowired
    private DefaultCommandResultProcessor commandResultProcessor;


    @Bean
    public DefaultCommandResultProcessor commandResultProcessor() {
        DefaultCommandResultProcessor processor = new DefaultCommandResultProcessor(6001);
        return processor;
    }

    @Bean
    public MysqlEventStore mysqlEventStore(HikariDataSource dataSource, IEventSerializer eventSerializer) {
        MysqlEventStore mysqlEventStore = new MysqlEventStore(dataSource, eventSerializer);
        return mysqlEventStore;
    }

    @Bean
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(HikariDataSource dataSource) {
        MysqlPublishedVersionStore publishedVersionStore = new MysqlPublishedVersionStore(dataSource);
        return publishedVersionStore;
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
