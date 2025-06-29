package com.whalefall541.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 示例定时任务
 * 演示Quartz Job的基本用法
 *
 * @author xx
 * @since 2024-07-10
 */
@Slf4j
@Component
public class SampleJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 获取任务参数
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();

        log.info("=== Quartz定时任务执行 ===");
        log.info("任务名称: {}", jobName);
        log.info("任务组: {}", jobGroup);
        log.info("执行时间: {}", currentTime);
        log.info("任务描述: {}", context.getJobDetail().getDescription());

        // 模拟业务处理
        try {
            Thread.sleep(1000); // 模拟耗时操作
            log.info("任务执行完成！");
        } catch (InterruptedException e) {
            log.error("任务执行被中断", e);
            Thread.currentThread().interrupt();
        }
    }
}
