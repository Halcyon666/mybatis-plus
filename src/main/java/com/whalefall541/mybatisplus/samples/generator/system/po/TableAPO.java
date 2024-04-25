package com.whalefall541.mybatisplus.samples.generator.system.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author xx
 * @since 2024-04-26
 */
@Getter
@Setter
@TableName("TABLE_A")
public class TableAPO extends Model<TableAPO> {

    /**
     * 编号
     */
    @TableField("ID")
    private Short id;

    /**
     * 姓名
     */
    @TableField("NAME")
    private String name;

    @Override
    public Serializable pkVal() {
        return null;
    }
}
