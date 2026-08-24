package com.basepro.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basepro.system.entity.SysMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询用户被授权的权限标识
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT DISTINCT m.permission
            FROM sys_menu m
                     JOIN sys_role_menu rm ON rm.menu_id = m.id AND rm.deleted = 0
                     JOIN sys_user_role ur ON ur.role_id = rm.role_id AND ur.deleted = 0
            WHERE ur.user_id = #{userId}
              AND m.deleted = 0
              AND m.status = 0
              AND m.permission IS NOT NULL
              AND m.permission <> ''
            """)
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 查询全部权限标识，超级管理员直接授予
     */
    @Select("""
            SELECT permission
            FROM sys_menu
            WHERE deleted = 0
              AND status = 0
              AND permission IS NOT NULL
              AND permission <> ''
            """)
    List<String> selectAllPermissions();

    /**
     * 查询用户可见的目录与菜单（不含按钮），用于前端生成路由
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT DISTINCT m.*
            FROM sys_menu m
                     JOIN sys_role_menu rm ON rm.menu_id = m.id AND rm.deleted = 0
                     JOIN sys_user_role ur ON ur.role_id = rm.role_id AND ur.deleted = 0
            WHERE ur.user_id = #{userId}
              AND m.deleted = 0
              AND m.status = 0
              AND m.type IN (1, 2)
            ORDER BY m.parent_id, m.sort
            """)
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 查询全部目录与菜单，超级管理员使用
     */
    @Select("""
            SELECT *
            FROM sys_menu
            WHERE deleted = 0
              AND status = 0
              AND type IN (1, 2)
            ORDER BY parent_id, sort
            """)
    List<SysMenu> selectAllMenus();

}
