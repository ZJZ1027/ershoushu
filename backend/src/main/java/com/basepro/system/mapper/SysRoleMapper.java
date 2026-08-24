package com.basepro.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basepro.system.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户拥有的角色标识（供 Spring Security 判角色 / 前端判断）
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT r.code
            FROM sys_user_role ur
                     JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
            WHERE ur.user_id = #{userId}
              AND ur.deleted = 0
              AND r.status = 0
            """)
    List<String> selectCodesByUserId(@Param("userId") Long userId);

}
