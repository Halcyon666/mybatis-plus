package com.whalefall541.service;

import com.whalefall541.constants.QuartzConstants;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Quartz调度器管理服务
 * 提供任务的创建、启动、停止、删除等功能
 *
 * @author xx
 * @since 2024-07-10
 */
@Slf4j
@Service
public class QuartzSchedulerService {

    private final Scheduler scheduler;

    public QuartzSchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 创建并启动一个简单的定时任务
     *
     * @param jobName     任务名称
     * @param jobGroup    任务组
     * @param jobClass    任务类
     * @param cronExpression Cron表达式
     * @param description 任务描述
     */
    public void scheduleJob(String jobName, String jobGroup, Class<? extends Job> jobClass,
                           String cronExpression, String description) {
        try {
            // 创建JobDetail
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName, jobGroup)
                    .withDescription(description)
                    .build();

            // 创建Trigger
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + QuartzConstants.TRIGGER_SUFFIX, jobGroup)
                    .withDescription(QuartzConstants.TRIGGER_DESC_PREFIX + description)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            // 调度任务
            scheduler.scheduleJob(jobDetail, trigger);

            log.info(QuartzConstants.LOG_JOB_CREATED, jobName, description, cronExpression);

        } catch (SchedulerException e) {
            log.error(QuartzConstants.ERROR_CREATE_JOB + ": {}", jobName, e);
            throw new RuntimeException(QuartzConstants.ERROR_CREATE_JOB, e);
        }
    }

    /**
     * 创建并启动一个简单的间隔任务
     *
     * @param jobName     任务名称
     * @param jobGroup    任务组
     * @param jobClass    任务类
     * @param intervalInSeconds 间隔秒数
     * @param description 任务描述
     */
    public void scheduleSimpleJob(String jobName, String jobGroup, Class<? extends Job> jobClass,
                                 int intervalInSeconds, String description) {
        try {
            // 创建JobDetail
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName, jobGroup)
                    .withDescription(description)
                    .build();

            // 创建SimpleTrigger
            SimpleTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + QuartzConstants.TRIGGER_SUFFIX, jobGroup)
                    .withDescription(QuartzConstants.TRIGGER_DESC_PREFIX + description)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds)
                            .repeatForever())
                    .build();

            // 调度任务
            scheduler.scheduleJob(jobDetail, trigger);

            log.info(QuartzConstants.LOG_SIMPLE_JOB_CREATED, jobName, description, intervalInSeconds);

        } catch (SchedulerException e) {
            log.error(QuartzConstants.ERROR_CREATE_SIMPLE_JOB + ": {}", jobName, e);
            throw new RuntimeException(QuartzConstants.ERROR_CREATE_SIMPLE_JOB, e);
        }
    }

    /**
     * 暂停任务
     */
    public void pauseJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.pauseJob(jobKey);
            log.info(QuartzConstants.LOG_JOB_PAUSED, jobGroup, jobName);
        } catch (SchedulerException e) {
            log.error(QuartzConstants.ERROR_PAUSE_JOB + ": {}.{}", jobGroup, jobName, e);
            throw new RuntimeException(QuartzConstants.ERROR_PAUSE_JOB, e);
        }
    }

    /**
     * 恢复任务
     */
    public void resumeJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.resumeJob(jobKey);
            log.info(QuartzConstants.LOG_JOB_RESUMED, jobGroup, jobName);
        } catch (SchedulerException e) {
            log.error(QuartzConstants.ERROR_RESUME_JOB + ": {}.{}", jobGroup, jobName, e);
            throw new RuntimeException(QuartzConstants.ERROR_RESUME_JOB, e);
        }
    }

    /**
     * 删除任务
     */
    public void deleteJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.deleteJob(jobKey);
            log.info(QuartzConstants.LOG_JOB_DELETED, jobGroup, jobName);
        } catch (SchedulerException e) {
            log.error(QuartzConstants.ERROR_DELETE_JOB + ": {}.{}", jobGroup, jobName, e);
            throw new RuntimeException(QuartzConstants.ERROR_DELETE_JOB, e);
        }
    }

    /**
     * 立即执行任务
     */
    public void triggerJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.triggerJob(jobKey);
            log.info(QuartzConstants.LOG_JOB_TRIGGERED, jobGroup, jobName);
        } catch (SchedulerException e) {
            log.error(QuartzConstants.ERROR_TRIGGER_JOB + ": {}.{}", jobGroup, jobName, e);
            throw new RuntimeException(QuartzConstants.ERROR_TRIGGER_JOB, e);
        }
    }

    /**
     * 检查任务是否存在
     */
    public boolean jobExists(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            log.error(QuartzConstants.ERROR_CHECK_JOB_EXISTS + ": {}.{}", jobGroup, jobName, e);
            return false;
        }
    }

    /**
     * 获取任务的下次执行时间
     */
    public Date getNextFireTime(String jobName, String jobGroup) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName + QuartzConstants.TRIGGER_SUFFIX, jobGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            return trigger != null ? trigger.getNextFireTime() : null;
        } catch (SchedulerException e) {
            log.error(QuartzConstants.ERROR_GET_NEXT_FIRE_TIME + ": {}.{}", jobGroup, jobName, e);
            return null;
        }
    }

    /**
     * 获取指定任务组中的所有任务名称
     * @param jobGroup 任务组名称
     * @return 任务名称列表
     */
    public List<String> getJobNamesInGroup(String jobGroup) {
        List<String> jobNames = new ArrayList<>();
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
            for (JobKey jobKey : jobKeys) {
                jobNames.add(jobKey.getName());
            }
            log.debug("获取任务组 {} 中的任务列表，共 {} 个任务", jobGroup, jobNames.size());
        } catch (SchedulerException e) {
            log.error("获取任务组 {} 中的任务列表失败", jobGroup, e);
        }
        return jobNames;
    }

    /**
     * 获取所有任务组名称
     * @return 任务组名称列表
     */
    public List<String> getAllJobGroupNames() {
        List<String> groupNames = new ArrayList<>();
        try {
            groupNames.addAll(scheduler.getJobGroupNames());
            log.debug("获取所有任务组列表，共 {} 个组", groupNames.size());
        } catch (SchedulerException e) {
            log.error("获取所有任务组列表失败", e);
        }
        return groupNames;
    }

    /**
     * 获取任务详细信息
     * @param jobName 任务名称
     * @param jobGroup 任务组名称
     * @return 任务详细信息
     */
    public JobDetail getJobDetail(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            return scheduler.getJobDetail(jobKey);
        } catch (SchedulerException e) {
            log.error("获取任务详细信息失败: {}.{}", jobGroup, jobName, e);
            return null;
        }
    }
}
