package com.whalefall541.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;

@Configuration
@Import({DynamicDataSourceAutoConfiguration.class})
public class DynamicDataSourceConfig {
    // The dynamic datasource starter will handle the configuration
}