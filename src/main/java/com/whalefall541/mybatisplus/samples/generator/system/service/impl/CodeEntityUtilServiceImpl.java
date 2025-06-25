package com.whalefall541.mybatisplus.samples.generator.system.service.impl;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.mapper.CodeEntityMapper;
import com.whalefall541.config.DataSourceUtils;
import com.whalefall541.config.DataSourceEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 *  使用工具类的服务实现类
 * </p>
 *
 * @author xx
 * @since 2024-07-10
 */
@Service
public class CodeEntityUtilServiceImpl extends ServiceImpl<CodeEntityMapper, CodeEntityPO> {

    /**
     * 使用工具类从主数据源查询
     * @return 查询结果
     */
    public List<CodeEntityPO> selectFromMasterWithUtil() {
        return DataSourceUtils.executeWithMaster(() -> this.list());
    }
    
    /**
     * 使用工具类从从数据源查询
     * @return 查询结果
     */
    public List<CodeEntityPO> selectFromSlaveWithUtil() {
        return DataSourceUtils.executeWithSlave(() -> this.list());
    }
    
    /**
     * 使用工具类动态切换数据源查询
     * @param dataSourceKey 数据源key
     * @return 查询结果
     */
    public List<CodeEntityPO> selectWithUtil(String dataSourceKey) {
        return DataSourceUtils.executeWithDataSource(dataSourceKey, () -> this.list());
    }
    
    /**
     * 使用工具类和枚举切换数据源查询
     * @param dataSourceEnum 数据源枚举
     * @return 查询结果
     */
    public List<CodeEntityPO> selectWithUtil(DataSourceEnum dataSourceEnum) {
        return DataSourceUtils.executeWithDataSource(dataSourceEnum, () -> this.list());
    }
    
    /**
     * 使用工具类保存到主数据源
     * @param entity 实体对象
     * @return 保存结果
     */
    public boolean saveToMasterWithUtil(CodeEntityPO entity) {
        return DataSourceUtils.executeWithMaster(() -> this.save(entity));
    }
    
    /**
     * 使用工具类保存到从数据源
     * @param entity 实体对象
     * @return 保存结果
     */
    public boolean saveToSlaveWithUtil(CodeEntityPO entity) {
        return DataSourceUtils.executeWithSlave(() -> this.save(entity));
    }
    
    /**
     * 复杂业务操作示例：读写分离
     * 从主库读取数据，处理后保存到从库
     * @param username 用户名
     * @return 操作结果描述
     */
    public String readWriteSeparationExample(String username) {
        // 从主库查询数据
        List<CodeEntityPO> masterData = DataSourceUtils.executeWithMaster(() -> {
            return this.lambdaQuery()
                    .eq(CodeEntityPO::getUsername, username)
                    .list();
        });
        
        if (masterData.isEmpty()) {
            return "主库中未找到用户数据: " + username;
        }
        
        // 处理数据并保存到从库
        CodeEntityPO processedEntity = masterData.get(0);
        processedEntity.setCode(processedEntity.getCode() + "_PROCESSED");
        
        boolean saveResult = DataSourceUtils.executeWithSlave(() -> {
            return this.save(processedEntity);
        });
        
        return String.format("处理完成 - 主库查询到 %d 条记录，从库保存结果: %s", 
                masterData.size(), saveResult ? "成功" : "失败");
    }
    
    /**
     * 数据同步示例：主库到从库
     * @return 同步结果
     */
    public String syncMasterToSlave() {
        // 从主库获取所有数据
        List<CodeEntityPO> masterData = DataSourceUtils.executeWithMaster(() -> this.list());
        
        if (masterData.isEmpty()) {
            return "主库无数据需要同步";
        }
        
        // 清空从库并同步数据
        DataSourceUtils.executeWithSlave(() -> {
            // 清空从库数据
            this.remove(null);
            
            // 批量插入主库数据
            this.saveBatch(masterData);
        });
        
        return String.format("同步完成：从主库同步了 %d 条记录到从库", masterData.size());
    }
    
    /**
     * 数据对比示例：比较主从库数据差异
     * @return 对比结果
     */
    public String compareDataSources() {
        List<CodeEntityPO> masterData = DataSourceUtils.executeWithMaster(() -> this.list());
        List<CodeEntityPO> slaveData = DataSourceUtils.executeWithSlave(() -> this.list());
        
        return String.format("数据对比结果 - 主库: %d 条记录, 从库: %d 条记录, 差异: %d 条", 
                masterData.size(), slaveData.size(), Math.abs(masterData.size() - slaveData.size()));
    }
}
