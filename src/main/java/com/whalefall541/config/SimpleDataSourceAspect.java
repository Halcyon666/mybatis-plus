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
 * 简化版数据源切换AOP切面
 * 更加健壮和简单的实现
 * 
 * @author xx
 * @since 2024-07-10
 */
@Aspect
@Component
@Order(1) // 确保在事务注解之前执行
public class SimpleDataSourceAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleDataSourceAspect.class);
    
    /**
     * 拦截@DataSourceSwitcher注解的方法
     */
    @Pointcut("@annotation(com.whalefall541.config.DataSourceSwitcher)")
    public void dataSourcePointCut() {
    }
    
    /**
     * 环绕通知，处理数据源切换
     */
    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String originalDataSource = DataSourceContextHolder.getDataSource();
        
        try {
            // 获取方法签名和注解
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            DataSourceSwitcher annotation = method.getAnnotation(DataSourceSwitcher.class);
            
            if (annotation != null) {
                String targetDataSource = determineDataSource(annotation);
                DataSourceContextHolder.setDataSource(targetDataSource);
                logger.debug("Switched to datasource: {} for method: {}", 
                    targetDataSource, method.getName());
            }
            
            // 执行目标方法
            return point.proceed();
            
        } catch (Exception e) {
            logger.error("Error in data source aspect: ", e);
            throw e;
        } finally {
            // 恢复原始数据源或清理
            if (originalDataSource != null) {
                DataSourceContextHolder.setDataSource(originalDataSource);
            } else {
                DataSourceContextHolder.clearDataSource();
            }
            logger.debug("Restored datasource context");
        }
    }
    
    /**
     * 确定要使用的数据源
     */
    private String determineDataSource(DataSourceSwitcher annotation) {
        String value = annotation.value();
        DataSourceEnum enumValue = annotation.dataSource();
        
        // 如果value不是默认值，优先使用value
        if (!"master".equals(value)) {
            return value;
        }
        
        // 否则使用枚举值
        return enumValue.getValue();
    }
}
