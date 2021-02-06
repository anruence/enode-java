package org.enodeframework.samples.commandhandles;

import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Vertx;
import org.enodeframework.jdbc.JDBCEventStore;
import org.enodeframework.jdbc.JDBCPublishedVersionStore;
import org.enodeframework.queue.DefaultSendReplyService;
import org.enodeframework.queue.command.DefaultCommandResultProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CommandConsumerAppConfig {

    @Value("${spring.enode.datasource.jdbcurl:}")
    private String jdbcUrl;

    @Value("${spring.enode.datasource.username:}")
    private String username;

    @Value("${spring.enode.datasource.password:}")
    private String password;

    @Bean("enodeMySQLDataSource")
    @ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "jdbc-mysql")
    public DataSource enodeMySQLDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @Autowired
    private DefaultCommandResultProcessor commandResultProcessor;

    @Autowired
    private DefaultSendReplyService sendReplyService;

    @Autowired
    private JDBCEventStore jdbcEventStore;

    @Autowired
    private JDBCPublishedVersionStore jdbcPublishedVersionStore;

    @Bean
    public Vertx vertx() {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(commandResultProcessor);
        vertx.deployVerticle(sendReplyService);
        vertx.deployVerticle(jdbcEventStore);
        vertx.deployVerticle(jdbcPublishedVersionStore);
        return vertx;
    }
}
