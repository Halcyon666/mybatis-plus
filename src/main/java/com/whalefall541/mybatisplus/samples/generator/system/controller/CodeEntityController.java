package com.whalefall541.mybatisplus.samples.generator.system.controller;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.CodeEntityServiceImpl;
import com.whalefall541.config.DataSourceEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xx
 * @since 2024-07-10
 */
@RestController
@RequestMapping("/system/codeEntity")
public class CodeEntityController {

    @Autowired
    private CodeEntityServiceImpl codeEntityService;

    /**
     * 从主数据源查询
     */
    @GetMapping("/master")
    public List<CodeEntityPO> selectFromMaster() {
        return codeEntityService.selectFromMaster();
    }

    /**
     * 从从数据源查询
     */
    @GetMapping("/slave")
    public List<CodeEntityPO> selectFromSlave() {
        return codeEntityService.selectFromSlave();
    }

    /**
     * 动态切换数据源查询
     * @param dataSource 数据源名称 (master/slave)
     */
    @GetMapping("/dynamic/{dataSource}")
    public List<CodeEntityPO> selectByDataSource(@PathVariable String dataSource) {
        return codeEntityService.selectByDataSource(dataSource);
    }

    /**
     * 使用枚举动态切换数据源查询
     * @param dataSource 数据源枚举 (MASTER/SLAVE)
     */
    @GetMapping("/enum/{dataSource}")
    public List<CodeEntityPO> selectByDataSourceEnum(@PathVariable String dataSource) {
        DataSourceEnum dataSourceEnum = DataSourceEnum.valueOf(dataSource.toUpperCase());
        return codeEntityService.selectByDataSource(dataSourceEnum);
    }

    /**
     * 动态切换数据源保存
     * @param entity 实体对象
     * @param dataSource 数据源名称
     */
    @PostMapping("/save/{dataSource}")
    public boolean saveByDataSource(@RequestBody CodeEntityPO entity, @PathVariable String dataSource) {
        return codeEntityService.saveByDataSource(entity, dataSource);
    }
}
