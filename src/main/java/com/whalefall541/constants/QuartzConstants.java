package com.whalefall541.constants;

/**
 * Quartz相关常量
 *
 * @author xx
 * @since 2024-07-10
 */
public final class QuartzConstants {
    
    private QuartzConstants() {
        // 私有构造函数，防止实例化
    }
    
    // 任务类型常量
    public static final String TYPE_EVERY_MINUTE = "every-minute";
    public static final String TYPE_EVERY_5_MINUTES = "every-5-minutes";
    public static final String TYPE_HOURLY = "hourly";
    public static final String TYPE_DAILY = "daily";
    public static final String TYPE_WEEKLY = "weekly";
    public static final String TYPE_MONTHLY = "monthly";
    public static final String TYPE_WORKDAYS = "workdays";
    public static final String TYPE_CUSTOM = "custom";
    
    // 触发器后缀
    public static final String TRIGGER_SUFFIX = "_trigger";
    
    // 响应消息常量
    public static final String MSG_SUCCESS = "success";
    public static final String MSG_ERROR = "error";
    public static final String MSG_MESSAGE = "message";
    
    // Cron表达式常量
    public static final String CRON_EVERY_MINUTE = "0 * * * * ?";
    public static final String CRON_EVERY_5_MINUTES = "0 */5 * * * ?";
    public static final String CRON_HOURLY = "0 0 * * * ?";
    public static final String CRON_DAILY = "0 0 9 * * ?";
    public static final String CRON_WEEKLY = "0 0 9 ? * MON";
    public static final String CRON_MONTHLY = "0 0 9 1 * ?";
    public static final String CRON_WORKDAYS = "0 0 9 ? * MON-FRI";
    
    // 任务描述常量
    public static final String DESC_EVERY_MINUTE = "每分钟执行的Cron任务";
    public static final String DESC_EVERY_5_MINUTES = "每5分钟执行的Cron任务";
    public static final String DESC_HOURLY = "每小时执行的Cron任务";
    public static final String DESC_DAILY = "每天上午9点执行的Cron任务";
    public static final String DESC_WEEKLY = "每周一上午9点执行的Cron任务";
    public static final String DESC_MONTHLY = "每月1号上午9点执行的Cron任务";
    public static final String DESC_WORKDAYS = "工作日上午9点执行的Cron任务";

    // 异常消息常量
    public static final String ERROR_CREATE_JOB = "创建定时任务失败";
    public static final String ERROR_CREATE_SIMPLE_JOB = "创建简单定时任务失败";
    public static final String ERROR_PAUSE_JOB = "暂停任务失败";
    public static final String ERROR_RESUME_JOB = "恢复任务失败";
    public static final String ERROR_DELETE_JOB = "删除任务失败";
    public static final String ERROR_TRIGGER_JOB = "手动触发任务失败";
    public static final String ERROR_GET_NEXT_FIRE_TIME = "获取任务下次执行时间失败";
    public static final String ERROR_CHECK_JOB_EXISTS = "检查任务是否存在失败";

    // 日志消息常量
    public static final String LOG_JOB_CREATED = "成功创建定时任务: {} - {}, Cron: {}";
    public static final String LOG_SIMPLE_JOB_CREATED = "成功创建简单定时任务: {} - {}, 间隔: {}秒";
    public static final String LOG_JOB_PAUSED = "任务已暂停: {}.{}";
    public static final String LOG_JOB_RESUMED = "任务已恢复: {}.{}";
    public static final String LOG_JOB_DELETED = "任务已删除: {}.{}";
    public static final String LOG_JOB_TRIGGERED = "任务已手动触发: {}.{}";

    // 触发器描述前缀
    public static final String TRIGGER_DESC_PREFIX = "触发器：";

    // 状态常量
    public static final String STATUS_ERROR = "ERROR";
}
