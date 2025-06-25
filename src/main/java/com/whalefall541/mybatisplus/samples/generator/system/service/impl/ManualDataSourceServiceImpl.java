package com.whalefall541.mybatisplus.samples.generator.system.service.impl;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.mapper.CodeEntityMapper;
import com.whalefall541.config.DataSourceContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 *  手动数据源切换服务实现类
 *  演示如何手动设置数据源（类似您图片中的用法）
 * </p>
 *
 * @author xx
 * @since 2024-07-10
 */
@Service
public class ManualDataSourceServiceImpl extends ServiceImpl<CodeEntityMapper, CodeEntityPO> {

    /**
     * 手动设置数据源方式1：在方法内部设置
     * 类似您图片中的用法
     */
    public boolean saveUser(CodeEntityPO entity) {
        // 手动设置数据源为master
        DataSourceContextHolder.setDataSource("master");
        try {
            // 执行数据库操作
            return this.save(entity);
        } finally {
            // 清理数据源上下文
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    /**
     * 手动设置数据源方式2：根据参数动态设置
     */
    public boolean saveUserToDataSource(CodeEntityPO entity, String dataSource) {
        DataSourceContextHolder.setDataSource(dataSource);
        try {
            return this.save(entity);
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    /**
     * 手动设置数据源方式3：查询操作
     */
    public List<CodeEntityPO> getUsersFromMaster() {
        DataSourceContextHolder.setDataSource("master");
        try {
            return this.list();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    /**
     * 手动设置数据源方式4：从从库查询
     */
    public List<CodeEntityPO> getUsersFromSlave() {
        DataSourceContextHolder.setDataSource("slave");
        try {
            return this.list();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    /**
     * 复杂业务操作：涉及多个数据源
     */
    public String complexBusinessOperation(CodeEntityPO entity) {
        StringBuilder result = new StringBuilder();
        
        // 1. 从主库查询现有数据
        DataSourceContextHolder.setDataSource("master");
        try {
            List<CodeEntityPO> masterData = this.list();
            result.append("主库数据量: ").append(masterData.size()).append("; ");
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
        
        // 2. 保存新数据到主库
        DataSourceContextHolder.setDataSource("master");
        try {
            boolean saveResult = this.save(entity);
            result.append("主库保存结果: ").append(saveResult ? "成功" : "失败").append("; ");
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
        
        // 3. 同步数据到从库
        DataSourceContextHolder.setDataSource("slave");
        try {
            // 修改实体的某些字段以区分
            entity.setCode(entity.getCode() + "_SYNC");
            boolean syncResult = this.save(entity);
            result.append("从库同步结果: ").append(syncResult ? "成功" : "失败");
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
        
        return result.toString();
    }
    
    /**
     * 使用AOP自动管理的方式（推荐）
     * AOP会自动清理数据源上下文
     */
    public List<CodeEntityPO> getUsersWithAutoCleanup(String dataSource) {
        // 只需要设置数据源，AOP会自动清理
        DataSourceContextHolder.setDataSource(dataSource);
        return this.list();
    }
    
    /**
     * 批量操作示例
     */
    public String batchOperationWithManualSwitch() {
        int masterCount = 0;
        int slaveCount = 0;
        
        // 统计主库数据
        DataSourceContextHolder.setDataSource("master");
        try {
            masterCount = this.list().size();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
        
        // 统计从库数据
        DataSourceContextHolder.setDataSource("slave");
        try {
            slaveCount = this.list().size();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
        
        return String.format("批量统计完成 - 主库: %d条, 从库: %d条, 差异: %d条", 
                masterCount, slaveCount, Math.abs(masterCount - slaveCount));
    }
}
