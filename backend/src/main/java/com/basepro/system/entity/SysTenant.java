package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {

    @TableId
    private Long id;

    @NotBlank(message = "租户名不能为空")
    private String name;

    private String contactName;

    private String contactMobile;

    private Integer status;

    private String domain;

    private LocalDateTime expireTime;

    private Integer accountCount;

    /**
     * 创建租户时同步创建的管理员账号，仅入参
     */
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String username;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

}
