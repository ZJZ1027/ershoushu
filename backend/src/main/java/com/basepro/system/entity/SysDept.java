package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    public static final long ROOT_PARENT_ID = 0L;

    @TableId
    private Long id;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 30, message = "部门名称长度不能超过 30 个字符")
    private String name;

    private Long parentId;

    private Integer sort;

    private Long leaderUserId;

    @Size(max = 11, message = "联系电话长度不能超过 11 位")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private Integer status;

    @TableField(exist = false)
    private List<SysDept> children;

}
