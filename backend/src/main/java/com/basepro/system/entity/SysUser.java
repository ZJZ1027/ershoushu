package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId
    private Long id;

    /**
     * 租户编号：由多租户插件在 SQL 层写入，因此不参与 insert / update
     */
    @JsonIgnore
    @TableField(value = "tenant_id", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Long tenantId;

    @NotBlank(message = "用户账号不能为空")
    @Size(max = 30, message = "用户账号长度不能超过 30 个字符")
    private String username;

    /**
     * 密码：只接收不返回
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称长度不能超过 30 个字符")
    private String nickname;

    private String remark;

    private Long deptId;

    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    @Size(max = 11, message = "手机号长度不能超过 11 位")
    private String mobile;

    private Integer sex;

    private String avatar;

    /** 待审核头像地址 */
    private String avatarPending;

    /** 头像审核状态：0无待审 1待审 2已驳回 */
    private Integer avatarAuditStatus;

    /** 头像驳回原因 */
    private String avatarRejectReason;

    @Size(max = 64, message = "微信号长度不能超过 64 个字符")
    private String wechat;

    @Size(max = 64, message = "校区长度不能超过 64 个字符")
    private String campus;

    @Size(max = 100, message = "个性签名长度不能超过 100 个字符")
    private String signature;

    private Integer status;

    private String loginIp;

    private LocalDateTime loginDate;

    /**
     * 所属部门名称，列表展示用
     */
    @TableField(exist = false)
    private String deptName;

    /**
     * 岗位编号，来自 sys_user_post
     */
    @TableField(exist = false)
    private List<Long> postIds;

    /**
     * 角色编号，来自 sys_user_role
     */
    @TableField(exist = false)
    private List<Long> roleIds;

}
