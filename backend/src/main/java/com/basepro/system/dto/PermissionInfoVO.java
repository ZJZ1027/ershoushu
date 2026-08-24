package com.basepro.system.dto;

import com.basepro.system.entity.SysMenu;

import java.util.List;

/**
 * 登录后的用户信息、权限与菜单，前端据此生成路由和按钮权限。
 */
public record PermissionInfoVO(UserInfo user,
                               List<String> roles,
                               List<String> permissions,
                               List<SysMenu> menus) {

    public record UserInfo(Long id, String username, String nickname, String avatar, Long deptId) {
    }

}
