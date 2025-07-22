package com.whalefall541.mybatisplus.samples.generator.system.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author xx
 * @since 2025-07-23
 */
@Getter
@Setter
@TableName("CODE_ENTITY")
@ToString
public class CodeEntityPO {

    @TableId("USERNAME")
    private String username;

    @TableField("CODE")
    private String code;

    @TableField("VALID")
    private String valid;

    @TableField("VERSION")
    private BigDecimal version;

    @TableField("CODE_VALID_TIME")
    private LocalDateTime codeValidTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;
}
