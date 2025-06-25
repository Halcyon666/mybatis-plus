package com.whalefall541.mybatisplus.samples.generator.system.service.impl;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.mapper.CodeEntityMapper;
import com.whalefall541.config.DataSourceContextHolder;
import com.whalefall541.config.DataSourceEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;
import java.util.List;
import java.io.Serializable;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xx
 * @since 2024-07-10
 */
@Service
public class CodeEntityServiceImpl extends ServiceImpl<CodeEntityMapper, CodeEntityPO> {

    /**
     * 使用主数据源查询
     * @return 查询结果
     */
    @DS("master")
    public List<CodeEntityPO> selectFromMaster() {
        return this.list();
    }

    /**
     * 使用从数据源查询
     * @return 查询结果
     */
    @DS("slave")
    public List<CodeEntityPO> selectFromSlave() {
        return this.list();
    }

    /**
     * 动态切换数据源查询
     * @param dataSourceKey 数据源key
     * @return 查询结果
     */
    public List<CodeEntityPO> selectByDataSource(String dataSourceKey) {
        try {
            DataSourceContextHolder.setDataSource(dataSourceKey);
            return this.list();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }

    /**
     * 使用枚举动态切换数据源查询
     * @param dataSourceEnum 数据源枚举
     * @return 查询结果
     */
    public List<CodeEntityPO> selectByDataSource(DataSourceEnum dataSourceEnum) {
        return selectByDataSource(dataSourceEnum.getValue());
    }

    /**
     * 动态切换数据源保存
     * @param entity 实体对象
     * @param dataSourceKey 数据源key
     * @return 保存结果
     */
    public boolean saveByDataSource(CodeEntityPO entity, String dataSourceKey) {
        try {
            DataSourceContextHolder.setDataSource(dataSourceKey);
            return this.save(entity);
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }

    /**
     * 动态切换数据源更新
     * @param entity 实体对象
     * @param dataSourceKey 数据源key
     * @return 更新结果
     */
    public boolean updateByDataSource(CodeEntityPO entity, String dataSourceKey) {
        try {
            DataSourceContextHolder.setDataSource(dataSourceKey);
            return this.updateById(entity);
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }

    /**
     * 动态切换数据源删除
     * @param id 主键
     * @param dataSourceKey 数据源key
     * @return 删除结果
     */
    public boolean deleteByDataSource(Serializable id, String dataSourceKey) {
        try {
            DataSourceContextHolder.setDataSource(dataSourceKey);
            return this.removeById(id);
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
}
