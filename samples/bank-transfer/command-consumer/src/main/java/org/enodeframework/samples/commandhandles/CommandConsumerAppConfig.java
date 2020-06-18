package org.enodeframework.samples.commandhandles;

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
public class CommandConsumerAppConfig {

    private Vertx vertx;
    @Autowired
    private MysqlEventStore mysqlEventStore;
    @Autowired
    private MysqlPublishedVersionStore publishedVersionStore;
    @Autowired
    private DefaultCommandResultProcessor commandResultProcessor;

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
    public MysqlEventStore mysqlEventStore(IEventSerializer eventSerializer, HikariDataSource dataSource) {
        return new MysqlEventStore(dataSource,eventSerializer);
    }

    @Bean
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(HikariDataSource dataSource) {
        return new MysqlPublishedVersionStore(dataSource);
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
