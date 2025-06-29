package com.whalefall541.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 简单测试任务
 * 用于验证Quartz是否正常工作
 *
 * @author xx
 * @since 2024-07-10
 */
@Slf4j
@Component
public class SimpleTestJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();

        // 使用醒目的日志格式
        log.info("🔥🔥🔥 QUARTZ任务执行中 🔥🔥🔥");
        log.info("⏰ 执行时间: {}", currentTime);
        log.info("📋 任务名称: {}", jobName);
        log.info("📁 任务组: {}", jobGroup);
        log.info("📝 任务描述: {}", context.getJobDetail().getDescription());

        // 模拟一些工作
        try {
            log.info("🔄 开始执行业务逻辑...");
            Thread.sleep(500); // 模拟耗时操作
            log.info("✅ 业务逻辑执行完成！");
        } catch (InterruptedException e) {
            log.error("❌ 任务执行被中断", e);
            Thread.currentThread().interrupt();
        }

        log.info("🎉 任务执行完毕！下次执行时间: {}", context.getTrigger().getNextFireTime());
    }
}
