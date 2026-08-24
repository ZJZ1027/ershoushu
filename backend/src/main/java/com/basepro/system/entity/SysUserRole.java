package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_role")
public class SysUserRole extends BaseEntity {

    @TableId
    private Long id;

    private Long userId;

    private Long roleId;

    public static SysUserRole of(Long userId, Long roleId) {
        SysUserRole entity = new SysUserRole();
        entity.setUserId(userId);
        entity.setRoleId(roleId);
        return entity;
    }

}
