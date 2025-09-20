package com.ncu.trackplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String h2Url;

    @Value("${spring.datasource.username}")
    private String h2Username;

    @Value("${spring.datasource.password}")
    private String h2Password;

    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(h2Url);
        dataSource.setUsername(h2Username);
        dataSource.setPassword(h2Password);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}