package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_menu")
public class SysRoleMenu extends BaseEntity {

    @TableId
    private Long id;

    private Long roleId;

    private Long menuId;

    public static SysRoleMenu of(Long roleId, Long menuId) {
        SysRoleMenu entity = new SysRoleMenu();
        entity.setRoleId(roleId);
        entity.setMenuId(menuId);
        return entity;
    }

}
