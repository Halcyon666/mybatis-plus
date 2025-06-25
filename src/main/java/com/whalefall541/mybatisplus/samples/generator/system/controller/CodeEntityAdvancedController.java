package com.whalefall541.mybatisplus.samples.generator.system.controller;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.CodeEntityAdvancedServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  高级前端控制器 - 演示注解方式数据源切换
 * </p>
 *
 * @author xx
 * @since 2024-07-10
 */
@RestController
@RequestMapping("/system/advanced")
public class CodeEntityAdvancedController {

    @Autowired
    private CodeEntityAdvancedServiceImpl advancedService;
    
    /**
     * 使用注解从主数据源查询
     */
    @GetMapping("/master/annotation")
    public List<CodeEntityPO> selectFromMasterWithAnnotation() {
        return advancedService.selectFromMasterWithAnnotation();
    }
    
    /**
     * 使用注解从从数据源查询
     */
    @GetMapping("/slave/annotation")
    public List<CodeEntityPO> selectFromSlaveWithAnnotation() {
        return advancedService.selectFromSlaveWithAnnotation();
    }
    
    /**
     * 使用枚举注解从主数据源查询
     */
    @GetMapping("/master/enum")
    public List<CodeEntityPO> selectFromMasterWithEnum() {
        return advancedService.selectFromMasterWithEnum();
    }
    
    /**
     * 使用枚举注解从从数据源查询
     */
    @GetMapping("/slave/enum")
    public List<CodeEntityPO> selectFromSlaveWithEnum() {
        return advancedService.selectFromSlaveWithEnum();
    }
    
    /**
     * 保存到主库
     */
    @PostMapping("/save/master")
    public boolean saveToMaster(@RequestBody CodeEntityPO entity) {
        return advancedService.saveToMaster(entity);
    }
    
    /**
     * 保存到从库
     */
    @PostMapping("/save/slave")
    public boolean saveToSlave(@RequestBody CodeEntityPO entity) {
        return advancedService.saveToSlave(entity);
    }
    
    /**
     * 批量操作演示
     */
    @PostMapping("/batch")
    public String batchOperation(@RequestBody CodeEntityPO entity) {
        return advancedService.batchOperation(entity);
    }
}
