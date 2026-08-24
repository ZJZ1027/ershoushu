package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.security.LoginUserService;
import com.basepro.system.dto.RoleQuery;
import com.basepro.system.entity.SysRole;
import com.basepro.system.entity.SysRoleMenu;
import com.basepro.system.entity.SysUserRole;
import com.basepro.system.mapper.SysRoleMapper;
import com.basepro.system.mapper.SysRoleMenuMapper;
import com.basepro.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色。租户内 name、code 唯一，内置角色不允许删除、也不允许改标识。
 */
@Service
@RequiredArgsConstructor
public class SysRoleService {

    /**
     * 自定义角色。新增的角色一律是自定义类型，内置角色只由初始化数据产生
     */
    private static final int TYPE_CUSTOM = 2;

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final LoginUserService loginUserService;

    public PageResult<SysRole> page(RoleQuery query) {
        Page<SysRole> page = roleMapper.selectPage(query.toPage(), Wrappers.<SysRole>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), SysRole::getName, query.getName())
                .like(StringUtils.hasText(query.getCode()), SysRole::getCode, query.getCode())
                .eq(query.getStatus() != null, SysRole::getStatus, query.getStatus())
                .ge(query.beginTime() != null, SysRole::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysRole::getCreateTime, query.endTime())
                .orderByAsc(SysRole::getSort));
        return PageResult.of(page);
    }

    public List<SysRole> simpleList() {
        return roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .select(SysRole::getId, SysRole::getName)
                .eq(SysRole::getStatus, 0)
                .orderByAsc(SysRole::getSort));
    }

    public SysRole get(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        return role;
    }

    public Long create(SysRole role) {
        role.setId(null);
        role.setType(TYPE_CUSTOM);
        validateUnique(role);
        roleMapper.insert(role);
        return role.getId();
    }

    public void update(SysRole role) {
        SysRole existing = get(role.getId());
        if (Integer.valueOf(SysRole.TYPE_BUILT_IN).equals(existing.getType())
                && !existing.getCode().equals(role.getCode())) {
            throw new BizException("内置角色不允许修改角色标识");
        }
        // 角色类型由系统维护，忽略前端传入的值
        role.setType(null);
        validateUnique(role);
        roleMapper.updateById(role);
        // 角色标识、状态的变化直接影响鉴权结果
        loginUserService.evictAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            SysRole role = get(id);
            if (Integer.valueOf(SysRole.TYPE_BUILT_IN).equals(role.getType())) {
                throw new BizException("内置角色不允许删除：" + role.getName());
            }
        }
        roleMapper.deleteByIds(ids);
        // 同步清理角色的菜单授权与用户关联，避免残留脏数据
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().in(SysRoleMenu::getRoleId, ids));
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getRoleId, ids));
        loginUserService.evictAll();
    }

    private void validateUnique(SysRole role) {
        Long count = roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getName, role.getName())
                .ne(role.getId() != null, SysRole::getId, role.getId()));
        if (count > 0) {
            throw new BizException("角色名称已存在");
        }
        count = roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, role.getCode())
                .ne(role.getId() != null, SysRole::getId, role.getId()));
        if (count > 0) {
            throw new BizException("角色标识已存在");
        }
    }

}
