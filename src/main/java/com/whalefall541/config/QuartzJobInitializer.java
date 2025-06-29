package com.whalefall541.config;

import com.whalefall541.job.SampleJob;
import com.whalefall541.service.QuartzSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Quartz任务初始化器
 * 在应用启动时自动创建一些测试任务
 *
 * @author xx
 * @since 2024-07-10
 */
@Slf4j
@Component
@Order(10) // 确保在Quartz启动之后执行
public class QuartzJobInitializer implements CommandLineRunner {

    private final QuartzSchedulerService quartzSchedulerService;

    public QuartzJobInitializer(QuartzSchedulerService quartzSchedulerService) {
        this.quartzSchedulerService = quartzSchedulerService;
    }

    @Override
    public void run(String... args) {
        log.info("开始初始化Quartz测试任务...");

        try {
            // 等待一下确保Quartz完全启动
            Thread.sleep(2000);

            // 创建示例任务 - 每30秒执行一次
            String sampleJobName = "autoSampleJob";
            String sampleJobGroup = "autoGroup";

            if (!quartzSchedulerService.jobExists(sampleJobName, sampleJobGroup)) {
                quartzSchedulerService.scheduleSimpleJob(
                    sampleJobName,
                    sampleJobGroup,
                    SampleJob.class,
                    60, // 30秒间隔
                    "自动创建的示例任务 - 每60秒执行一次"
                );
                log.info("✅ 自动创建示例任务成功: {}.{}", sampleJobGroup, sampleJobName);
            } else {
                log.info("示例任务已存在，跳过创建");
            }

            log.info("🎉 Quartz任务初始化完成！请观察控制台日志查看任务执行情况。");

        } catch (InterruptedException e) {
            log.error("❌ 初始化Quartz任务被中断", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("❌ 初始化Quartz任务失败", e);
        }
    }
}
