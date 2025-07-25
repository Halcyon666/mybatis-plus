package com.whalefall541.mybatisplus.samples.generator.system.service.impl;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.mapper.CodeEntityMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xx
 * @since 2025-07-23
 */
@Service
public class CodeEntityServiceImpl extends ServiceImpl<CodeEntityMapper, CodeEntityPO> {

    @Transactional(rollbackFor = Exception.class)
    public CodeEntityPO getByIdMine(String username) {

        return baseMapper.selectById(username);
    }
}
