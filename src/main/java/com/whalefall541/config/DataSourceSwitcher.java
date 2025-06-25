package com.whalefall541.config;

import java.lang.annotation.*;

/**
 * 数据源切换注解
 * 
 * @author xx
 * @since 2024-07-10
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSourceSwitcher {
    
    /**
     * 数据源名称
     * @return 数据源key
     */
    String value() default "master";
    
    /**
     * 数据源枚举
     * @return 数据源枚举
     */
    DataSourceEnum dataSource() default DataSourceEnum.MASTER;
}
