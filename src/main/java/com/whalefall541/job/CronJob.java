package com.whalefall541.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cron定时任务
 * 演示使用Cron表达式的定时任务
 *
 * @author xx
 * @since 2024-07-10
 */
@Slf4j
@Component
public class CronJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();
        String description = context.getJobDetail().getDescription();
        
        // 获取触发器信息
        String triggerName = context.getTrigger().getKey().getName();
        String cronExpression = context.getTrigger() instanceof org.quartz.CronTrigger ? 
            ((org.quartz.CronTrigger) context.getTrigger()).getCronExpression() : "N/A";
        
        // 使用醒目的日志格式
        log.info("⏰⏰⏰ CRON任务执行中 ⏰⏰⏰");
        log.info("🕐 执行时间: {}", currentTime);
        log.info("📋 任务名称: {}", jobName);
        log.info("📁 任务组: {}", jobGroup);
        log.info("📝 任务描述: {}", description);
        log.info("🔧 触发器: {}", triggerName);
        log.info("📅 Cron表达式: {}", cronExpression);
        
        // 根据任务类型执行不同的业务逻辑
        try {
            if (jobName.contains("daily")) {
                executeDailyTask();
            } else if (jobName.contains("hourly")) {
                executeHourlyTask();
            } else if (jobName.contains("minute")) {
                executeMinuteTask();
            } else if (jobName.contains("weekly")) {
                executeWeeklyTask();
            } else if (jobName.contains("monthly")) {
                executeMonthlyTask();
            } else if (jobName.contains("workdays")) {
                executeWorkdayTask();
            } else {
                executeGenericTask();
            }
            
            log.info("✅ Cron任务执行完成！");
            
        } catch (Exception e) {
            log.error("❌ Cron任务执行失败", e);
            throw new JobExecutionException("Cron任务执行失败", e);
        }
        
        // 显示下次执行时间
        if (context.getTrigger().getNextFireTime() != null) {
            String nextFireTime = context.getTrigger().getNextFireTime()
                .toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.info("⏭️ 下次执行时间: {}", nextFireTime);
        }
        
        log.info("===================================================================================================="); // 分隔线
    }
    
    private void executeDailyTask() {
        log.info("🌅 执行每日任务 - 数据备份、报表生成等");
        simulateWork(1000);
    }
    
    private void executeHourlyTask() {
        log.info("⏰ 执行每小时任务 - 系统监控、缓存清理等");
        simulateWork(500);
    }
    
    private void executeMinuteTask() {
        log.info("⚡ 执行分钟级任务 - 实时数据同步等");
        simulateWork(200);
    }
    
    private void executeWeeklyTask() {
        log.info("📊 执行每周任务 - 周报生成、系统维护等");
        simulateWork(2000);
    }
    
    private void executeMonthlyTask() {
        log.info("📈 执行每月任务 - 月度统计、账单生成等");
        simulateWork(3000);
    }
    
    private void executeWorkdayTask() {
        log.info("💼 执行工作日任务 - 业务数据处理等");
        simulateWork(800);
    }
    
    private void executeGenericTask() {
        log.info("🔄 执行通用任务 - 自定义业务逻辑");
        simulateWork(600);
    }
    
    private void simulateWork(long millis) {
        try {
            log.info("🔄 开始执行业务逻辑...");
            Thread.sleep(millis);
            log.info("✅ 业务逻辑执行完成！耗时: {}ms", millis);
        } catch (InterruptedException e) {
            log.error("❌ 任务执行被中断", e);
            Thread.currentThread().interrupt();
        }
    }
}
