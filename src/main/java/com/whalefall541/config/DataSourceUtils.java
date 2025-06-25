package com.whalefall541.config;

import java.util.function.Supplier;

/**
 * 数据源工具类
 * 提供便捷的数据源切换方法
 * 
 * @author xx
 * @since 2024-07-10
 */
public class DataSourceUtils {
    
    /**
     * 在指定数据源中执行操作
     * @param dataSourceKey 数据源key
     * @param supplier 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public static <T> T executeWithDataSource(String dataSourceKey, Supplier<T> supplier) {
        try {
            DataSourceContextHolder.setDataSource(dataSourceKey);
            return supplier.get();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    /**
     * 在指定数据源中执行操作（使用枚举）
     * @param dataSourceEnum 数据源枚举
     * @param supplier 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public static <T> T executeWithDataSource(DataSourceEnum dataSourceEnum, Supplier<T> supplier) {
        return executeWithDataSource(dataSourceEnum.getValue(), supplier);
    }
    
    /**
     * 在主数据源中执行操作
     * @param supplier 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public static <T> T executeWithMaster(Supplier<T> supplier) {
        return executeWithDataSource(DataSourceEnum.MASTER, supplier);
    }
    
    /**
     * 在从数据源中执行操作
     * @param supplier 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public static <T> T executeWithSlave(Supplier<T> supplier) {
        return executeWithDataSource(DataSourceEnum.SLAVE, supplier);
    }
    
    /**
     * 在指定数据源中执行无返回值操作
     * @param dataSourceKey 数据源key
     * @param runnable 要执行的操作
     */
    public static void executeWithDataSource(String dataSourceKey, Runnable runnable) {
        try {
            DataSourceContextHolder.setDataSource(dataSourceKey);
            runnable.run();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    /**
     * 在指定数据源中执行无返回值操作（使用枚举）
     * @param dataSourceEnum 数据源枚举
     * @param runnable 要执行的操作
     */
    public static void executeWithDataSource(DataSourceEnum dataSourceEnum, Runnable runnable) {
        executeWithDataSource(dataSourceEnum.getValue(), runnable);
    }
    
    /**
     * 在主数据源中执行无返回值操作
     * @param runnable 要执行的操作
     */
    public static void executeWithMaster(Runnable runnable) {
        executeWithDataSource(DataSourceEnum.MASTER, runnable);
    }
    
    /**
     * 在从数据源中执行无返回值操作
     * @param runnable 要执行的操作
     */
    public static void executeWithSlave(Runnable runnable) {
        executeWithDataSource(DataSourceEnum.SLAVE, runnable);
    }
}
