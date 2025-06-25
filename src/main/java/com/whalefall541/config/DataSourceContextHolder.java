package com.whalefall541.config;

/**
 * 数据源上下文持有者
 * 用于在运行时动态切换数据源
 *
 * @author xx
 * @since 2024-07-10
 */
public class DataSourceContextHolder {
    private DataSourceContextHolder() {
    }

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置数据源
     * @param dataSourceKey 数据源key
     */
    public static void setDataSource(String dataSourceKey) {
        CONTEXT_HOLDER.set(dataSourceKey);
    }

    /**
     * 获取当前数据源
     * @return 数据源key
     */
    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除数据源
     */
    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }
}
