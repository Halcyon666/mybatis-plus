package com.whalefall541.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化版数据源配置
 * 如果dynamic-datasource依赖有问题，可以使用这个配置
 *
 * @author xx
 * @since 2024-07-10
 */
@Configuration
public class SimpleDataSourceConfig {

    /**
     * 主数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.dynamic.datasource.master")
    public DataSource masterDataSource() {
        return new HikariDataSource();
    }

    /**
     * 从数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.dynamic.datasource.slave")
    public DataSource slaveDataSource() {
        return new HikariDataSource();
    }

    /**
     * 动态数据源
     * 方法一
     */
    @Primary
    @Bean
    public DataSource dynamicDataSource() {
        SimpleDynamicDataSource dynamicDataSource = new SimpleDynamicDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource());
        dataSourceMap.put("slave", slaveDataSource());

        // 设置数据源映射
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        // 设置默认数据源
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource());

        return dynamicDataSource;
    }
}
