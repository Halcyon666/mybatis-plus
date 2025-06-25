package com.whalefall541.mybatisplus.samples.generator.system.controller;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.ManualDataSourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  手动数据源切换控制器
 * </p>
 *
 * @author xx
 * @since 2024-07-10
 */
@RestController
@RequestMapping("/system/manual")
public class ManualDataSourceController {

    @Autowired
    private ManualDataSourceServiceImpl manualService;
    
    /**
     * 手动保存用户到主库
     */
    @PostMapping("/save/user")
    public boolean saveUser(@RequestBody CodeEntityPO entity) {
        return manualService.saveUser(entity);
    }
    
    /**
     * 手动保存用户到指定数据源
     */
    @PostMapping("/save/user/{dataSource}")
    public boolean saveUserToDataSource(@RequestBody CodeEntityPO entity, @PathVariable String dataSource) {
        return manualService.saveUserToDataSource(entity, dataSource);
    }
    
    /**
     * 从主库查询用户
     */
    @GetMapping("/users/master")
    public List<CodeEntityPO> getUsersFromMaster() {
        return manualService.getUsersFromMaster();
    }
    
    /**
     * 从从库查询用户
     */
    @GetMapping("/users/slave")
    public List<CodeEntityPO> getUsersFromSlave() {
        return manualService.getUsersFromSlave();
    }
    
    /**
     * 复杂业务操作
     */
    @PostMapping("/complex")
    public String complexBusinessOperation(@RequestBody CodeEntityPO entity) {
        return manualService.complexBusinessOperation(entity);
    }
    
    /**
     * 使用AOP自动管理的查询
     */
    @GetMapping("/users/auto/{dataSource}")
    public List<CodeEntityPO> getUsersWithAutoCleanup(@PathVariable String dataSource) {
        return manualService.getUsersWithAutoCleanup(dataSource);
    }
    
    /**
     * 批量操作示例
     */
    @GetMapping("/batch/stats")
    public String batchOperationWithManualSwitch() {
        return manualService.batchOperationWithManualSwitch();
    }
}
