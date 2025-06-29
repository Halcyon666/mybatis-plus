package com.whalefall541.controller;

import com.whalefall541.constants.QuartzConstants;
import com.whalefall541.job.CronJob;
import com.whalefall541.job.SimpleTestJob;
import com.whalefall541.service.QuartzSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Quartz测试控制器
 * 用于测试Quartz调度器是否正常工作
 *
 * @author xx
 * @since 2024-07-10
 */
@Slf4j
@RestController
@RequestMapping("/api/quartz")
public class QuartzTestController {

    private final Scheduler scheduler;
    private final QuartzSchedulerService quartzSchedulerService;

    public QuartzTestController(Scheduler scheduler, QuartzSchedulerService quartzSchedulerService) {
        this.scheduler = scheduler;
        this.quartzSchedulerService = quartzSchedulerService;
    }

    /**
     * 测试Quartz调度器状态
     */
    @GetMapping("/status")
    public Map<String, Object> getSchedulerStatus() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put(QuartzConstants.MSG_SUCCESS, true);
            result.put("schedulerName", scheduler.getSchedulerName());
            result.put("schedulerInstanceId", scheduler.getSchedulerInstanceId());
            result.put("isStarted", scheduler.isStarted());
            result.put("isInStandbyMode", scheduler.isInStandbyMode());
            result.put("isShutdown", scheduler.isShutdown());
            result.put("metaData", scheduler.getMetaData().getSummary());

            log.info("Quartz调度器状态检查成功");

        } catch (SchedulerException e) {
            log.error("获取Quartz调度器状态失败", e);
            result.put(QuartzConstants.MSG_SUCCESS, false);
            result.put(QuartzConstants.MSG_ERROR, e.getMessage());
        }
        return result;
    }

    /**
     * 简单的健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean isStarted = scheduler.isStarted();
            result.put(QuartzConstants.MSG_SUCCESS, true);
            result.put("status", isStarted ? "UP" : "DOWN");
            result.put(QuartzConstants.MSG_MESSAGE, isStarted ? "Quartz调度器运行正常" : "Quartz调度器未启动");

        } catch (Exception e) {
            log.error("Quartz健康检查失败", e);
            result.put(QuartzConstants.MSG_SUCCESS, false);
            result.put("status", QuartzConstants.STATUS_ERROR);
            result.put(QuartzConstants.MSG_ERROR, e.getMessage());
        }
        return result;
    }

    /**
     * 立即创建并执行一个测试任务
     */
    @PostMapping("/run-now")
    public Map<String, Object> runTestJobNow() {
        Map<String, Object> result = new HashMap<>();
        try {
            String jobName = "immediateTestJob";
            String jobGroup = "immediateGroup";

            // 先删除可能存在的任务
            if (quartzSchedulerService.jobExists(jobName, jobGroup)) {
                quartzSchedulerService.deleteJob(jobName, jobGroup);
            }

            // 创建一个立即执行的任务
            quartzSchedulerService.scheduleSimpleJob(
                jobName,
                jobGroup,
                SimpleTestJob.class,
                60, // 60秒间隔（但我们会立即触发）
                "立即执行的测试任务"
            );

            // 立即触发任务
            quartzSchedulerService.triggerJob(jobName, jobGroup);

            result.put(QuartzConstants.MSG_SUCCESS, true);
            result.put(QuartzConstants.MSG_MESSAGE, "测试任务已创建并立即执行，请查看控制台日志");
            result.put("jobName", jobName);
            result.put("jobGroup", jobGroup);

            log.info("🚀 立即执行测试任务: {}.{}", jobGroup, jobName);

        } catch (Exception e) {
            log.error("创建立即执行任务失败", e);
            result.put(QuartzConstants.MSG_SUCCESS, false);
            result.put(QuartzConstants.MSG_ERROR, e.getMessage());
        }
        return result;
    }

    /**
     * 创建Cron定时任务
     * 支持多种常用的Cron表达式
     */
    @PostMapping("/create-cron-job")
    public Map<String, Object> createCronJob(@RequestParam(defaultValue = "daily") String type,
                                           @RequestParam(required = false) String customCron) {
        Map<String, Object> result = new HashMap<>();
        try {
            String jobName = "cronJob_" + type;
            String jobGroup = "cronGroup";
            String cronExpression;
            String description;

            // 根据类型选择Cron表达式
            switch (type.toLowerCase()) {
                case QuartzConstants.TYPE_EVERY_MINUTE:
                    cronExpression = QuartzConstants.CRON_EVERY_MINUTE;
                    description = QuartzConstants.DESC_EVERY_MINUTE;
                    break;
                case QuartzConstants.TYPE_EVERY_5_MINUTES:
                    cronExpression = QuartzConstants.CRON_EVERY_5_MINUTES;
                    description = QuartzConstants.DESC_EVERY_5_MINUTES;
                    break;
                case QuartzConstants.TYPE_HOURLY:
                    cronExpression = QuartzConstants.CRON_HOURLY;
                    description = QuartzConstants.DESC_HOURLY;
                    break;
                case QuartzConstants.TYPE_DAILY:
                    cronExpression = QuartzConstants.CRON_DAILY;
                    description = QuartzConstants.DESC_DAILY;
                    break;
                case QuartzConstants.TYPE_WEEKLY:
                    cronExpression = QuartzConstants.CRON_WEEKLY;
                    description = QuartzConstants.DESC_WEEKLY;
                    break;
                case QuartzConstants.TYPE_MONTHLY:
                    cronExpression = QuartzConstants.CRON_MONTHLY;
                    description = QuartzConstants.DESC_MONTHLY;
                    break;
                case QuartzConstants.TYPE_WORKDAYS:
                    cronExpression = QuartzConstants.CRON_WORKDAYS;
                    description = QuartzConstants.DESC_WORKDAYS;
                    break;
                case QuartzConstants.TYPE_CUSTOM:
                    if (customCron == null || customCron.trim().isEmpty()) {
                        result.put(QuartzConstants.MSG_SUCCESS, false);
                        result.put(QuartzConstants.MSG_ERROR, "自定义Cron表达式不能为空");
                        return result;
                    }
                    cronExpression = customCron.trim();
                    description = "自定义Cron任务: " + cronExpression;
                    jobName = "cronJob_custom";
                    break;
                default:
                    result.put(QuartzConstants.MSG_SUCCESS, false);
                    result.put(QuartzConstants.MSG_ERROR, "不支持的任务类型: " + type);
                    result.put("supportedTypes", new String[]{
                        QuartzConstants.TYPE_EVERY_MINUTE, QuartzConstants.TYPE_EVERY_5_MINUTES,
                        QuartzConstants.TYPE_HOURLY, QuartzConstants.TYPE_DAILY,
                        QuartzConstants.TYPE_WEEKLY, QuartzConstants.TYPE_MONTHLY,
                        QuartzConstants.TYPE_WORKDAYS, QuartzConstants.TYPE_CUSTOM
                    });
                    return result;
            }

            // 先删除可能存在的同名任务
            if (quartzSchedulerService.jobExists(jobName, jobGroup)) {
                quartzSchedulerService.deleteJob(jobName, jobGroup);
                log.info("删除已存在的任务: {}.{}", jobGroup, jobName);
            }

            // 创建Cron任务
            quartzSchedulerService.scheduleJob(
                jobName,
                jobGroup,
                CronJob.class, // 使用专门的CronJob类
                cronExpression,
                description
            );

            result.put(QuartzConstants.MSG_SUCCESS, true);
            result.put(QuartzConstants.MSG_MESSAGE, "Cron任务创建成功");
            result.put("jobName", jobName);
            result.put("jobGroup", jobGroup);
            result.put("cronExpression", cronExpression);
            result.put("description", description);
            result.put("nextFireTime", quartzSchedulerService.getNextFireTime(jobName, jobGroup));

            log.info("🕐 成功创建Cron任务: {} - {}", jobName, cronExpression);

        } catch (Exception e) {
            log.error("创建Cron任务失败", e);
            result.put(QuartzConstants.MSG_SUCCESS, false);
            result.put(QuartzConstants.MSG_ERROR, e.getMessage());
        }
        return result;
    }

    /**
     * 获取Cron表达式帮助信息
     */
    @GetMapping("/cron-help")
    public Map<String, Object> getCronHelp() {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> examples = new HashMap<>();

        examples.put("0 * * * * ?", "每分钟执行");
        examples.put("0 */5 * * * ?", "每5分钟执行");
        examples.put("0 0 * * * ?", "每小时执行");
        examples.put("0 0 9 * * ?", "每天上午9点执行");
        examples.put("0 0 9 ? * MON", "每周一上午9点执行");
        examples.put("0 0 9 1 * ?", "每月1号上午9点执行");
        examples.put("0 0 9 ? * MON-FRI", "工作日上午9点执行");
        examples.put("0 30 10,14,16 * * ?", "每天10:30、14:30、16:30执行");
        examples.put("0 0 12 ? * WED", "每周三中午12点执行");
        examples.put("0 0 12 1/15 * ?", "每月1号和15号中午12点执行");

        // 特殊字符说明
        Map<String, String> specialCharacters = new HashMap<>();
        specialCharacters.put("*", "匹配所有值");
        specialCharacters.put("?", "不指定值（用于日和周）");
        specialCharacters.put("-", "范围（如1-5）");
        specialCharacters.put(",", "列举（如1,3,5）");
        specialCharacters.put("/", "步长（如0/15表示从0开始每15分钟）");
        specialCharacters.put("L", "最后（如6L表示最后一个周五）");
        specialCharacters.put("W", "工作日");
        specialCharacters.put("#", "第几个（如6#3表示第3个周五）");

        // 预设类型说明
        Map<String, String> presetTypes = new HashMap<>();
        presetTypes.put(QuartzConstants.TYPE_EVERY_MINUTE, "每分钟执行");
        presetTypes.put(QuartzConstants.TYPE_EVERY_5_MINUTES, "每5分钟执行");
        presetTypes.put(QuartzConstants.TYPE_HOURLY, "每小时执行");
        presetTypes.put(QuartzConstants.TYPE_DAILY, "每天执行");
        presetTypes.put(QuartzConstants.TYPE_WEEKLY, "每周执行");
        presetTypes.put(QuartzConstants.TYPE_MONTHLY, "每月执行");
        presetTypes.put(QuartzConstants.TYPE_WORKDAYS, "工作日执行");
        presetTypes.put(QuartzConstants.TYPE_CUSTOM, "自定义表达式");

        result.put(QuartzConstants.MSG_SUCCESS, true);
        result.put("format", "秒 分 时 日 月 周");
        result.put("examples", examples);
        result.put("specialCharacters", specialCharacters);
        result.put("presetTypes", presetTypes);

        return result;
    }

    /**
     * 删除指定的任务
     * @param jobName 任务名称
     * @param jobGroup 任务组（可选，默认为defaultGroup）
     */
    @DeleteMapping("/delete-job")
    public Map<String, Object> deleteJob(
            @RequestParam String jobName,
            @RequestParam(defaultValue = "defaultGroup") String jobGroup) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 检查任务是否存在
            if (!quartzSchedulerService.jobExists(jobName, jobGroup)) {
                result.put(QuartzConstants.MSG_SUCCESS, false);
                result.put(QuartzConstants.MSG_ERROR, "任务不存在: " + jobGroup + "." + jobName);
                return result;
            }

            // 删除任务
            quartzSchedulerService.deleteJob(jobName, jobGroup);

            result.put(QuartzConstants.MSG_SUCCESS, true);
            result.put(QuartzConstants.MSG_MESSAGE, "任务删除成功");
            result.put("jobName", jobName);
            result.put("jobGroup", jobGroup);
            result.put("deletedAt", new Date());

            log.info("🗑️ 成功删除任务: {}.{}", jobGroup, jobName);

        } catch (Exception e) {
            log.error("删除任务失败: {}.{}", jobGroup, jobName, e);
            result.put(QuartzConstants.MSG_SUCCESS, false);
            result.put(QuartzConstants.MSG_ERROR, e.getMessage());
        }

        return result;
    }

    /**
     * 批量删除任务
     * @param jobNames 任务名称列表，格式：jobGroup.jobName 或 jobName（使用默认组）
     */
    @DeleteMapping("/delete-jobs")
    public Map<String, Object> deleteJobs(@RequestParam List<String> jobNames) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> deleteResults = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (String jobIdentifier : jobNames) {
            Map<String, Object> jobResult = new HashMap<>();
            String jobName;
            String jobGroup;

            try {
                // 解析任务标识符
                if (jobIdentifier.contains(".")) {
                    String[] parts = jobIdentifier.split("\\.", 2);
                    jobGroup = parts[0];
                    jobName = parts[1];
                } else {
                    jobGroup = "defaultGroup";
                    jobName = jobIdentifier;
                }

                jobResult.put("jobName", jobName);
                jobResult.put("jobGroup", jobGroup);
                jobResult.put("identifier", jobIdentifier);

                // 检查任务是否存在
                if (!quartzSchedulerService.jobExists(jobName, jobGroup)) {
                    jobResult.put(QuartzConstants.MSG_SUCCESS, false);
                    jobResult.put(QuartzConstants.MSG_ERROR, "任务不存在");
                    failCount++;
                } else {
                    // 删除任务
                    quartzSchedulerService.deleteJob(jobName, jobGroup);
                    jobResult.put(QuartzConstants.MSG_SUCCESS, true);
                    jobResult.put(QuartzConstants.MSG_MESSAGE, "删除成功");
                    successCount++;
                    log.info("🗑️ 批量删除任务成功: {}.{}", jobGroup, jobName);
                }

            } catch (Exception e) {
                log.error("批量删除任务失败: {}", jobIdentifier, e);
                jobResult.put(QuartzConstants.MSG_SUCCESS, false);
                jobResult.put(QuartzConstants.MSG_ERROR, e.getMessage());
                failCount++;
            }

            deleteResults.add(jobResult);
        }

        result.put(QuartzConstants.MSG_SUCCESS, failCount == 0);
        result.put(QuartzConstants.MSG_MESSAGE,
            String.format("批量删除完成：成功 %d 个，失败 %d 个", successCount, failCount));
        result.put("totalCount", jobNames.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("results", deleteResults);
        result.put("deletedAt", new Date());

        log.info("🗑️ 批量删除任务完成：总数 {}, 成功 {}, 失败 {}",
            jobNames.size(), successCount, failCount);

        return result;
    }

    /**
     * 删除指定组的所有任务
     * @param jobGroup 任务组名称
     */
    @DeleteMapping("/delete-group")
    public Map<String, Object> deleteJobGroup(@RequestParam String jobGroup) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 获取组中的所有任务
            List<String> jobNames = quartzSchedulerService.getJobNamesInGroup(jobGroup);

            if (jobNames.isEmpty()) {
                result.put(QuartzConstants.MSG_SUCCESS, false);
                result.put(QuartzConstants.MSG_ERROR, "任务组不存在或为空: " + jobGroup);
                return result;
            }

            int successCount = 0;
            int failCount = 0;
            List<String> deletedJobs = new ArrayList<>();
            List<String> failedJobs = new ArrayList<>();

            // 逐个删除任务
            for (String jobName : jobNames) {
                try {
                    quartzSchedulerService.deleteJob(jobName, jobGroup);
                    deletedJobs.add(jobName);
                    successCount++;
                    log.info("🗑️ 删除组任务成功: {}.{}", jobGroup, jobName);
                } catch (Exception e) {
                    log.error("删除组任务失败: {}.{}", jobGroup, jobName, e);
                    failedJobs.add(jobName);
                    failCount++;
                }
            }

            result.put(QuartzConstants.MSG_SUCCESS, failCount == 0);
            result.put(QuartzConstants.MSG_MESSAGE,
                String.format("删除任务组完成：成功 %d 个，失败 %d 个", successCount, failCount));
            result.put("jobGroup", jobGroup);
            result.put("totalCount", jobNames.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("deletedJobs", deletedJobs);
            result.put("failedJobs", failedJobs);
            result.put("deletedAt", new Date());

            log.info("🗑️ 删除任务组完成: {} - 总数 {}, 成功 {}, 失败 {}",
                jobGroup, jobNames.size(), successCount, failCount);

        } catch (Exception e) {
            log.error("删除任务组失败: {}", jobGroup, e);
            result.put(QuartzConstants.MSG_SUCCESS, false);
            result.put(QuartzConstants.MSG_ERROR, e.getMessage());
        }

        return result;
    }

    /**
     * 获取所有任务列表
     */
    @GetMapping("/list-jobs")
    public Map<String, Object> listAllJobs() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<String> groupNames = quartzSchedulerService.getAllJobGroupNames();
            Map<String, List<Map<String, Object>>> jobsByGroup = new HashMap<>();
            int totalJobs = 0;

            for (String groupName : groupNames) {
                List<String> jobNames = quartzSchedulerService.getJobNamesInGroup(groupName);
                List<Map<String, Object>> jobInfos = new ArrayList<>();

                for (String jobName : jobNames) {
                    Map<String, Object> jobInfo = new HashMap<>();
                    jobInfo.put("jobName", jobName);
                    jobInfo.put("jobGroup", groupName);
                    jobInfo.put("identifier", groupName + "." + jobName);
                    jobInfo.put("exists", quartzSchedulerService.jobExists(jobName, groupName));
                    jobInfo.put("nextFireTime", quartzSchedulerService.getNextFireTime(jobName, groupName));

                    // 获取任务详细信息
                    JobDetail jobDetail = quartzSchedulerService.getJobDetail(jobName, groupName);
                    if (jobDetail != null) {
                        jobInfo.put("jobClass", jobDetail.getJobClass().getSimpleName());
                        jobInfo.put("description", jobDetail.getDescription());
                        jobInfo.put("durable", jobDetail.isDurable());
                        jobInfo.put("requestsRecovery", jobDetail.requestsRecovery());
                    }

                    jobInfos.add(jobInfo);
                    totalJobs++;
                }

                jobsByGroup.put(groupName, jobInfos);
            }

            result.put(QuartzConstants.MSG_SUCCESS, true);
            result.put("totalGroups", groupNames.size());
            result.put("totalJobs", totalJobs);
            result.put("jobsByGroup", jobsByGroup);
            result.put("groupNames", groupNames);
            result.put("queriedAt", new Date());

            log.info("📋 查询任务列表成功：共 {} 个组，{} 个任务", groupNames.size(), totalJobs);

        } catch (Exception e) {
            log.error("查询任务列表失败", e);
            result.put(QuartzConstants.MSG_SUCCESS, false);
            result.put(QuartzConstants.MSG_ERROR, e.getMessage());
        }

        return result;
    }

    /**
     * 获取指定组的任务列表
     * @param jobGroup 任务组名称
     */
    @GetMapping("/list-jobs/{jobGroup}")
    public Map<String, Object> listJobsInGroup(@PathVariable String jobGroup) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<String> jobNames = quartzSchedulerService.getJobNamesInGroup(jobGroup);
            List<Map<String, Object>> jobInfos = new ArrayList<>();

            for (String jobName : jobNames) {
                Map<String, Object> jobInfo = new HashMap<>();
                jobInfo.put("jobName", jobName);
                jobInfo.put("jobGroup", jobGroup);
                jobInfo.put("identifier", jobGroup + "." + jobName);
                jobInfo.put("exists", quartzSchedulerService.jobExists(jobName, jobGroup));
                jobInfo.put("nextFireTime", quartzSchedulerService.getNextFireTime(jobName, jobGroup));

                // 获取任务详细信息
                JobDetail jobDetail = quartzSchedulerService.getJobDetail(jobName, jobGroup);
                if (jobDetail != null) {
                    jobInfo.put("jobClass", jobDetail.getJobClass().getSimpleName());
                    jobInfo.put("description", jobDetail.getDescription());
                    jobInfo.put("durable", jobDetail.isDurable());
                    jobInfo.put("requestsRecovery", jobDetail.requestsRecovery());
                }

                jobInfos.add(jobInfo);
            }

            result.put(QuartzConstants.MSG_SUCCESS, true);
            result.put("jobGroup", jobGroup);
            result.put("jobCount", jobNames.size());
            result.put("jobs", jobInfos);
            result.put("queriedAt", new Date());

            log.info("📋 查询任务组 {} 成功：共 {} 个任务", jobGroup, jobNames.size());

        } catch (Exception e) {
            log.error("查询任务组 {} 失败", jobGroup, e);
            result.put(QuartzConstants.MSG_SUCCESS, false);
            result.put(QuartzConstants.MSG_ERROR, e.getMessage());
        }

        return result;
    }
}
