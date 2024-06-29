package com.whalefall541.mybatisplus.samples.generator.system.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author xx
 * @since 2024-06-29
 */
@Getter
@Setter
@TableName("JWT_USERS")
public class JwtUsersPO {

    @TableId("USERNAME")
    private String username;

    @TableField("PASSWORD")
    private String password;

    @TableField("ROLE")
    private String role;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;
}
