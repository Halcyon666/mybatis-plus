package com.whalefall541.job;

import com.whalefall541.config.DataSourceContextHolder;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.CodeEntityAdvancedServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 数据库操作定时任务
 * 演示在Quartz Job中使用动态数据源
 *
 * @author xx
 * @since 2024-07-10
 */
@Slf4j
@Component
public class DatabaseJob implements Job {

    @Autowired
    private CodeEntityAdvancedServiceImpl codeEntityService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String jobName = context.getJobDetail().getKey().getName();
        
        log.info("=== 数据库操作定时任务开始执行 ===");
        log.info("任务名称: {}", jobName);
        log.info("执行时间: {}", currentTime);
        
        try {
            // 演示切换到主数据源查询
            DataSourceContextHolder.setDataSource("master");
            log.info("切换到主数据源(Oracle)进行查询...");
            int masterCount = codeEntityService.selectFromMasterWithAnnotation().size();
            log.info("主数据源查询结果数量: {}", masterCount);
            
            // 演示切换到从数据源查询
            DataSourceContextHolder.setDataSource("slave");
            log.info("切换到从数据源(MySQL)进行查询...");
            int slaveCount = codeEntityService.selectFromSlaveWithAnnotation().size();
            log.info("从数据源查询结果数量: {}", slaveCount);
            
            log.info("数据库操作定时任务执行完成！");
            
        } catch (Exception e) {
            log.error("数据库操作定时任务执行失败", e);
            throw new JobExecutionException("数据库操作失败", e);
        } finally {
            // 清理数据源上下文
            DataSourceContextHolder.clearDataSource();
        }
    }
}
