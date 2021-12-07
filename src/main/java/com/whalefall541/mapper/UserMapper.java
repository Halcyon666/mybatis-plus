package com.whalefall541.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whalefall541.entity.table.Users;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: WhaleFall541
 * @date: 2021/12/7 0:37
 */
@Mapper
public interface UserMapper extends BaseMapper<Users> {
}
