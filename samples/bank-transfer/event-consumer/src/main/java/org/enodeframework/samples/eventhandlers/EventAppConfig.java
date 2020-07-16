package org.enodeframework.samples.eventhandlers;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.enodeframework.samples.QueueProperties.JDBC_URL;

@Configuration
public class EventAppConfig {

    @Bean(name = "enodeMysqlDataSource")
    public HikariDataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(JDBC_URL);
        dataSource.setUsername("root");
        dataSource.setPassword("abcd1234&ABCD");
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }
}
