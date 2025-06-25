package com.whalefall541.mybatisplus.samples.generator.system.controller;

import com.whalefall541.config.DataSourceContextHolder;
import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.CodeEntityAdvancedServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
@RequestMapping
public class CodeEntityAdvancedController {

    @Resource
    private CodeEntityAdvancedServiceImpl advancedService;

    /**
     * 使用注解从主数据源查询
     */
    @GetMapping("/master/handleSet")
    public List<CodeEntityPO> selectFromMasterWithAnnotation() {
        try {
            DataSourceContextHolder.setDataSource("master");
            return advancedService.selectFromMasterWithAnnotation();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }

    /**
     * 使用注解从从数据源查询
     */
    @GetMapping("/slave/handleSet")
    public List<CodeEntityPO> selectFromSlaveWithAnnotation() {
        try {
            DataSourceContextHolder.setDataSource("slave");

            return advancedService.selectFromSlaveWithAnnotation();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
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

}
