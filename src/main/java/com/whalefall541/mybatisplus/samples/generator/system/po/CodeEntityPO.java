package com.whalefall541.mybatisplus.samples.generator.system.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author xx
 * @since 2024-07-10
 */
@Getter
@Setter
@TableName("CODE_ENTITY")
public class CodeEntityPO {

    @TableField("USERNAME")
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
