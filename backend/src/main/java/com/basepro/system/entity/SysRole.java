package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /**
     * 内置角色，不允许删除
     */
    public static final int TYPE_BUILT_IN = 1;

    @TableId
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过 30 个字符")
    private String name;

    @NotBlank(message = "角色标识不能为空")
    @Size(max = 100, message = "角色标识长度不能超过 100 个字符")
    private String code;

    private Integer sort;

    private Integer status;

    private Integer type;

    private String remark;

}
