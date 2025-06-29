package com.whalefall541.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

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
@Order(-10)
public class SimpleDataSourceConfig {

    /**
     * 主数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.dynamic.datasource.master")
    public DataSource masterDataSource() {
        return applyCommonPoolConfig(new org.apache.tomcat.jdbc.pool.DataSource(), "SELECT 1 FROM DUAL");
    }

    /**
     * 从数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.dynamic.datasource.slave")
    public DataSource slaveDataSource() {

        return applyCommonPoolConfig(new org.apache.tomcat.jdbc.pool.DataSource(), "SELECT 1");
    }

    private org.apache.tomcat.jdbc.pool.DataSource applyCommonPoolConfig(org.apache.tomcat.jdbc.pool.DataSource ds, String validationQuery) {
        ds.setInitialSize(5);
        ds.setMaxActive(30);
        ds.setMinIdle(3);
        ds.setMaxIdle(10);
        ds.setMaxWait(30000);

        ds.setTestOnBorrow(true);
        ds.setTestWhileIdle(true); // 建议开启，避免闲置连接意外失效
        ds.setValidationQuery(validationQuery);
        ds.setValidationQueryTimeout(5); // 秒

        ds.setTimeBetweenEvictionRunsMillis(30000); // 30秒扫描一次空闲连接
        ds.setMinEvictableIdleTimeMillis(60000); // 60秒空闲就回收

        ds.setRemoveAbandoned(true);
        ds.setRemoveAbandonedTimeout(60); // 60秒未关闭被认为泄漏
        ds.setLogAbandoned(true);

        ds.setValidationInterval(30000); // 最小间隔30秒执行一次验证

        return ds;
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
