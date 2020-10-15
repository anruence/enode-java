package com.microsoft.conference.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.MySqlDialect;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@MapperScan(basePackages = {"com.microsoft.conference"},
        sqlSessionFactoryRef = "conferenceSqlSessionFactory"
)
public class ConferenceDataSourceConfiguration {

    public static String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/conference";

    @Bean("paginationInterceptor")
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        paginationInterceptor.setLimit(-1);
        paginationInterceptor.setDbType(DbType.MYSQL);
        paginationInterceptor.setDialect(new MySqlDialect());
        paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
        return paginationInterceptor;
    }

    @Bean(name = "conferenceSqlDataSource")
    public DataSource conferenceSqlDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(JDBC_URL);
        dataSource.setUsername("root");
        dataSource.setPassword("abcd1234&ABCD");
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @Bean(name = "enodeMySQLDataSource")
    public DataSource enodeMySQLDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(JDBC_URL);
        dataSource.setUsername("root");
        dataSource.setPassword("abcd1234&ABCD");
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        return dataSource;
    }

    @Bean(name = "conferenceSqlSessionFactory")
    public SqlSessionFactory conferenceSqlSessionFactory(
            @Qualifier("conferenceSqlDataSource") DataSource conferenceSqlDataSource,
            PaginationInterceptor paginationInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        // 默认使用数据库db自增
        dbConfig.setIdType(IdType.ASSIGN_ID);
        globalConfig.setDbConfig(dbConfig);
        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        sqlSessionFactoryBean.setPlugins(paginationInterceptor);
        sqlSessionFactoryBean.setDataSource(conferenceSqlDataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "conferenceSqlTransactionManager")
    public DataSourceTransactionManager conferenceSqlTransactionManager(
            @Qualifier("conferenceSqlDataSource") DataSource conferenceSqlDataSource) {
        return new DataSourceTransactionManager(conferenceSqlDataSource);
    }
}
