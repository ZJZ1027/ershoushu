package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.common.BizException;
import com.basepro.security.LoginUserService;
import com.basepro.system.dto.AssignRoleMenuReq;
import com.basepro.system.dto.AssignUserRoleReq;
import com.basepro.system.entity.SysRole;
import com.basepro.system.entity.SysRoleMenu;
import com.basepro.system.entity.SysUserRole;
import com.basepro.system.mapper.SysRoleMapper;
import com.basepro.system.mapper.SysRoleMenuMapper;
import com.basepro.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 授权：角色与菜单、用户与角色的关联维护。两个 assign 都是全量覆盖（先删后插）。
 */
@Service
@RequiredArgsConstructor
public class SysPermissionService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final LoginUserService loginUserService;

    public Set<Long> listRoleMenus(Long roleId) {
        if (roleId == null) {
            throw new BizException("角色编号不能为空");
        }
        validateRolesExist(List.of(roleId));
        return roleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery()
                        .select(SysRoleMenu::getMenuId)
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenu(AssignRoleMenuReq request) {
        validateRolesExist(List.of(request.roleId()));
        Set<Long> menuIds = distinct(request.menuIds());
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, request.roleId()));
        menuIds.forEach(menuId -> roleMenuMapper.insert(SysRoleMenu.of(request.roleId(), menuId)));
        loginUserService.evictAll();
    }

    public Set<Long> listUserRoles(Long userId) {
        if (userId == null) {
            throw new BizException("用户编号不能为空");
        }
        return userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .select(SysUserRole::getRoleId)
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toSet());
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignUserRole(AssignUserRoleReq request) {
        Set<Long> roleIds = distinct(request.roleIds());
        validateRolesExist(roleIds);
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, request.userId()));
        roleIds.forEach(roleId -> userRoleMapper.insert(SysUserRole.of(request.userId(), roleId)));
        loginUserService.evictAll();
    }

    /**
     * 去重并剔除 null，前端多选可能带上空值
     */
    private Set<Long> distinct(Set<Long> ids) {
        Set<Long> result = new HashSet<>();
        if (ids != null) {
            ids.stream().filter(Objects::nonNull).forEach(result::add);
        }
        return result;
    }

    /**
     * 校验角色都存在。带租户条件查询，顺带拦住跨租户授权
     */
    private void validateRolesExist(Collection<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }
        Long count = roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery().in(SysRole::getId, roleIds));
        if (count == null || count != roleIds.size()) {
            throw new BizException("角色不存在");
        }
    }

}
