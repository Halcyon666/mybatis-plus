package com.whalefall541.config;

/**
 * 数据源枚举
 * 
 * @author xx
 * @since 2024-07-10
 */
public enum DataSourceEnum {
    
    /**
     * 主数据源
     */
    MASTER("master"),
    
    /**
     * 从数据源
     */
    SLAVE("slave");
    
    private final String value;
    
    DataSourceEnum(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
