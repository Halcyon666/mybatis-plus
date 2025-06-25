package com.whalefall541.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 简化版动态数据源
 * 如果dynamic-datasource依赖有问题，可以使用这个简化版本
 * 
 * @author xx
 * @since 2024-07-10
 */
public class SimpleDynamicDataSource extends AbstractRoutingDataSource {
    
    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = DataSourceContextHolder.getDataSource();
        System.out.println("Current DataSource: " + dataSource);
        return dataSource;
    }
}
