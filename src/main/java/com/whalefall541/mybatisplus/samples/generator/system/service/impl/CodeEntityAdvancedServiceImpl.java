package com.whalefall541.mybatisplus.samples.generator.system.service.impl;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.mapper.CodeEntityMapper;
import com.whalefall541.config.DataSourceSwitcher;
import com.whalefall541.config.DataSourceEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 *  高级服务实现类 - 使用注解方式切换数据源
 * </p>
 *
 * @author xx
 * @since 2024-07-10
 */
@Service
public class CodeEntityAdvancedServiceImpl extends ServiceImpl<CodeEntityMapper, CodeEntityPO> {

    /**
     * 使用注解切换到主数据源查询
     * @return 查询结果
     */
    @DataSourceSwitcher(value = "master")
    public List<CodeEntityPO> selectFromMasterWithAnnotation() {
        return this.list();
    }
    
    /**
     * 使用注解切换到从数据源查询
     * @return 查询结果
     */
    @DataSourceSwitcher(value = "slave")
    public List<CodeEntityPO> selectFromSlaveWithAnnotation() {
        return this.list();
    }
    
    /**
     * 使用枚举注解切换到主数据源查询
     * @return 查询结果
     */
    @DataSourceSwitcher(dataSource = DataSourceEnum.MASTER)
    public List<CodeEntityPO> selectFromMasterWithEnum() {
        return this.list();
    }
    
    /**
     * 使用枚举注解切换到从数据源查询
     * @return 查询结果
     */
    @DataSourceSwitcher(dataSource = DataSourceEnum.SLAVE)
    public List<CodeEntityPO> selectFromSlaveWithEnum() {
        return this.list();
    }
    
    /**
     * 使用注解切换数据源保存
     * @param entity 实体对象
     * @return 保存结果
     */
    @DataSourceSwitcher(value = "master")
    public boolean saveToMaster(CodeEntityPO entity) {
        return this.save(entity);
    }
    
    /**
     * 使用注解切换数据源保存到从库
     * @param entity 实体对象
     * @return 保存结果
     */
    @DataSourceSwitcher(value = "slave")
    public boolean saveToSlave(CodeEntityPO entity) {
        return this.save(entity);
    }
    
    /**
     * 批量操作 - 主库查询，从库保存
     * @param entity 实体对象
     * @return 操作结果
     */
    public String batchOperation(CodeEntityPO entity) {
        // 从主库查询
        List<CodeEntityPO> masterData = selectFromMasterWithAnnotation();
        
        // 保存到从库
        boolean saveResult = saveToSlave(entity);
        
        return String.format("Master count: %d, Save to slave: %s", 
                masterData.size(), saveResult ? "Success" : "Failed");
    }
}
