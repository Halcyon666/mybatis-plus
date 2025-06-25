package com.whalefall541.mybatisplus.samples.generator.system.controller;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.CodeEntityUtilServiceImpl;
import com.whalefall541.config.DataSourceEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  工具类使用演示控制器
 * </p>
 *
 * @author xx
 * @since 2024-07-10
 */
@RestController
@RequestMapping("/system/util")
public class CodeEntityUtilController {

    @Autowired
    private CodeEntityUtilServiceImpl utilService;
    
    /**
     * 使用工具类从主数据源查询
     */
    @GetMapping("/master")
    public List<CodeEntityPO> selectFromMasterWithUtil() {
        return utilService.selectFromMasterWithUtil();
    }
    
    /**
     * 使用工具类从从数据源查询
     */
    @GetMapping("/slave")
    public List<CodeEntityPO> selectFromSlaveWithUtil() {
        return utilService.selectFromSlaveWithUtil();
    }
    
    /**
     * 使用工具类动态切换数据源查询
     * @param dataSource 数据源名称
     */
    @GetMapping("/dynamic/{dataSource}")
    public List<CodeEntityPO> selectWithUtil(@PathVariable String dataSource) {
        return utilService.selectWithUtil(dataSource);
    }
    
    /**
     * 使用工具类和枚举切换数据源查询
     * @param dataSource 数据源枚举名称
     */
    @GetMapping("/enum/{dataSource}")
    public List<CodeEntityPO> selectWithUtilEnum(@PathVariable String dataSource) {
        DataSourceEnum dataSourceEnum = DataSourceEnum.valueOf(dataSource.toUpperCase());
        return utilService.selectWithUtil(dataSourceEnum);
    }
    
    /**
     * 保存到主数据源
     */
    @PostMapping("/save/master")
    public boolean saveToMasterWithUtil(@RequestBody CodeEntityPO entity) {
        return utilService.saveToMasterWithUtil(entity);
    }
    
    /**
     * 保存到从数据源
     */
    @PostMapping("/save/slave")
    public boolean saveToSlaveWithUtil(@RequestBody CodeEntityPO entity) {
        return utilService.saveToSlaveWithUtil(entity);
    }
    
    /**
     * 读写分离示例
     * @param username 用户名
     */
    @GetMapping("/read-write-separation/{username}")
    public String readWriteSeparationExample(@PathVariable String username) {
        return utilService.readWriteSeparationExample(username);
    }
    
    /**
     * 数据同步示例
     */
    @PostMapping("/sync")
    public String syncMasterToSlave() {
        return utilService.syncMasterToSlave();
    }
    
    /**
     * 数据对比示例
     */
    @GetMapping("/compare")
    public String compareDataSources() {
        return utilService.compareDataSources();
    }
}
