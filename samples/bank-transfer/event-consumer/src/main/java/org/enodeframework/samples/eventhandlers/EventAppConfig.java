package org.enodeframework.samples.eventhandlers;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import org.enodeframework.ENodeBootstrap;
import org.enodeframework.eventing.impl.InMemoryEventStore;
import org.enodeframework.eventing.impl.InMemoryPublishedVersionStore;
import org.enodeframework.mysql.MysqlEventStore;
import org.enodeframework.mysql.MysqlPublishedVersionStore;
import org.enodeframework.queue.command.CommandResultProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.enodeframework.samples.QueueProperties.JDBC_URL;

@Configuration
public class EventAppConfig {
    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("org.enodeframework.samples"));
        return bootstrap;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor(6001);
        return processor;
    }

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
    public MysqlEventStore mysqlEventStore(HikariDataSource dataSource) {
        MysqlEventStore mysqlEventStore = new MysqlEventStore(dataSource, null);
        return mysqlEventStore;
    }

    @Bean
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(HikariDataSource dataSource) {
        return new MysqlPublishedVersionStore(dataSource, null);
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

}
