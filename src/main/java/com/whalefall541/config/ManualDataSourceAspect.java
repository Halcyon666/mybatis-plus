package com.whalefall541.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 手动数据源切换AOP切面
 * 拦截DataSourceContextHolder.setDataSource()调用，实现自动数据源切换
 * 
 * @author xx
 * @since 2024-07-10
 */
@Aspect
@Component
@Order(0) // 确保在其他切面之前执行
public class ManualDataSourceAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(ManualDataSourceAspect.class);
    
    /**
     * 拦截Service层的方法执行
     */
    @Pointcut("execution(* com.whalefall541.mybatisplus.samples.generator.system.service.impl.*.*(..))")
    public void serviceMethodPointCut() {
    }
    
    @Around("serviceMethodPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().getName();
        String className = point.getTarget().getClass().getSimpleName();

        // 只对包含"WithAutoCleanup"的方法进行自动清理
        boolean shouldAutoCleanup = methodName.contains("WithAutoCleanup") ||
                                   methodName.contains("AutoManaged");

        logger.debug("Executing method: {}.{}, auto cleanup: {}", className, methodName, shouldAutoCleanup);

        try {
            Object result = point.proceed();

            if (shouldAutoCleanup) {
                String currentDataSource = DataSourceContextHolder.getDataSource();
                if (currentDataSource != null) {
                    logger.info("Method {}.{} executed with datasource: {}", className, methodName, currentDataSource);
                }
            }

            return result;
        } finally {
            // 只对特定方法进行自动清理
            if (shouldAutoCleanup) {
                String currentDataSource = DataSourceContextHolder.getDataSource();
                if (currentDataSource != null) {
                    DataSourceContextHolder.clearDataSource();
                    logger.debug("Auto-cleared datasource context after method: {}.{}", className, methodName);
                }
            }
        }
    }
}
