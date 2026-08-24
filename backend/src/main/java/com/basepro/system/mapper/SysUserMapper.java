package com.basepro.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basepro.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 按编号查询用户，跳过租户过滤：令牌里的用户编号本身已经全局唯一，
     * 且认证发生在租户上下文建立之前。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM sys_user WHERE id = #{id} AND deleted = 0")
    SysUser selectByIdIgnoreTenant(@Param("id") Long id);

}
