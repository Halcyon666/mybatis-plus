package com.whalefall541.config;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

/**
 * Quartz调度器配置
 * 使用MySQL数据源作为Quartz的持久化存储
 *
 * @author xx
 * @since 2024-07-10
 */
@Configuration
public class QuartzConfig {

    /**
     * 配置调度器工厂Bean
     * 明确设置数据源和属性
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("slaveDataSource") DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        // 设置数据源
        factory.setDataSource(dataSource);

        // 设置调度器名称
        factory.setSchedulerName("QuartzScheduler");

        // 设置应用上下文
        factory.setApplicationContextSchedulerContextKey("applicationContext");

        // 设置覆盖已存在的任务
        factory.setOverwriteExistingJobs(true);

        // 设置自动启动
        factory.setAutoStartup(true);

        // 设置延迟启动时间（秒）
        factory.setStartupDelay(15);

        return factory;
    }

    /**
     * 获取调度器
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getScheduler();
    }
}
