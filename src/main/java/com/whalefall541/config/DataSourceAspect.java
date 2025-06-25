package com.whalefall541.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据源切换AOP切面
 * 
 * @author xx
 * @since 2024-07-10
 */
@Aspect
@Component
@Order(1) // 确保在事务注解之前执行
public class DataSourceAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);
    
    @Pointcut("@annotation(com.whalefall541.config.DataSourceSwitcher)")
    public void dataSourcePointCut() {
    }
    
    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DataSourceSwitcher dataSourceSwitcher = method.getAnnotation(DataSourceSwitcher.class);

        if (dataSourceSwitcher != null) {
            String dataSourceKey = dataSourceSwitcher.value();

            // 优先使用value值，如果value是默认值则使用枚举值
            if (!"master".equals(dataSourceKey)) {
                // 如果value不是默认的master，直接使用value
                DataSourceContextHolder.setDataSource(dataSourceKey);
                logger.info("Switch to datasource by value: {}", dataSourceKey);
            } else {
                // 如果value是默认值，检查是否设置了枚举值
                DataSourceEnum enumValue = dataSourceSwitcher.dataSource();
                if (enumValue != DataSourceEnum.MASTER) {
                    // 使用枚举值
                    DataSourceContextHolder.setDataSource(enumValue.getValue());
                    logger.info("Switch to datasource by enum: {}", enumValue.getValue());
                } else {
                    // 使用默认的master
                    DataSourceContextHolder.setDataSource("master");
                    logger.info("Switch to datasource by default: master");
                }
            }
        }

        try {
            return point.proceed();
        } finally {
            DataSourceContextHolder.clearDataSource();
            logger.debug("Clear datasource context");
        }
    }
}
