package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.system.dto.TenantQuery;
import com.basepro.system.entity.SysMenu;
import com.basepro.system.entity.SysRole;
import com.basepro.system.entity.SysRoleMenu;
import com.basepro.system.entity.SysTenant;
import com.basepro.system.entity.SysToken;
import com.basepro.system.entity.SysUser;
import com.basepro.system.entity.SysUserRole;
import com.basepro.system.mapper.SysMenuMapper;
import com.basepro.system.mapper.SysRoleMapper;
import com.basepro.system.mapper.SysRoleMenuMapper;
import com.basepro.system.mapper.SysTenantMapper;
import com.basepro.system.mapper.SysTokenMapper;
import com.basepro.system.mapper.SysUserMapper;
import com.basepro.system.mapper.SysUserRoleMapper;
import com.basepro.security.LoginUserService;
import com.basepro.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 租户。sys_tenant 是全局表（无 tenant_id），只有平台管理员可以维护。
 * <p>
 * 新增租户时会在新租户的上下文里初始化「管理员角色 + 管理员账号 + 菜单授权」，
 * 否则新租户登录进来将是一个没有任何菜单的空壳。
 */
@Service
@RequiredArgsConstructor
public class SysTenantService {

    private static final String ADMIN_NAME = "管理员";

    private static final String ADMIN_ROLE_CODE = "tenant_admin";

    /**
     * 角色类型：自定义
     */
    private static final int ROLE_TYPE_CUSTOM = 2;

    /**
     * 状态：正常
     */
    private static final int STATUS_ENABLE = 0;

    /**
     * 租户管理属于平台级功能，不授权给租户
     */
    private static final String PLATFORM_PERMISSION_PREFIX = "system:tenant:";

    private static final String PLATFORM_MENU_PATH = "/system/tenant";

    private final SysTenantMapper tenantMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysTokenMapper tokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginUserService loginUserService;

    public PageResult<SysTenant> page(TenantQuery query) {
        Page<SysTenant> page = tenantMapper.selectPage(query.toPage(), Wrappers.<SysTenant>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), SysTenant::getName, query.getName())
                .like(StringUtils.hasText(query.getContactName()), SysTenant::getContactName, query.getContactName())
                .like(StringUtils.hasText(query.getContactMobile()), SysTenant::getContactMobile,
                        query.getContactMobile())
                .eq(query.getStatus() != null, SysTenant::getStatus, query.getStatus())
                .ge(query.beginTime() != null, SysTenant::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysTenant::getCreateTime, query.endTime())
                .orderByDesc(SysTenant::getId));
        return PageResult.of(page);
    }

    public List<SysTenant> simpleList() {
        return tenantMapper.selectList(Wrappers.<SysTenant>lambdaQuery()
                .select(SysTenant::getId, SysTenant::getName)
                .eq(SysTenant::getStatus, STATUS_ENABLE)
                .orderByAsc(SysTenant::getId));
    }

    public SysTenant get(Long id) {
        SysTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BizException("租户不存在");
        }
        return tenant;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(SysTenant tenant) {
        tenant.setId(null);
        validateNameUnique(tenant);
        if (!StringUtils.hasText(tenant.getUsername()) || !StringUtils.hasText(tenant.getPassword())) {
            throw new BizException("管理员账号与密码不能为空");
        }
        tenantMapper.insert(tenant);
        initTenant(tenant.getId(), tenant.getUsername(), tenant.getPassword());
        return tenant.getId();
    }

    public void update(SysTenant tenant) {
        get(tenant.getId());
        validateNameUnique(tenant);
        tenantMapper.updateById(tenant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        if (ids.contains(TenantContext.DEFAULT_TENANT_ID)) {
            throw new BizException("默认租户不允许删除");
        }
        tenantMapper.deleteByIds(ids);
        ids.forEach(this::clearTenantData);
    }

    /**
     * 租户删掉后，它的账号、角色也要一并删除并踢下线，否则会留下一批登不进来、
     * 也不在任何列表里出现的孤儿数据
     */
    private void clearTenantData(Long tenantId) {
        Long originTenantId = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            List<Long> userIds = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                            .select(SysUser::getId))
                    .stream()
                    .map(SysUser::getId)
                    .toList();
            if (!userIds.isEmpty()) {
                userMapper.deleteByIds(userIds);
                userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getUserId, userIds));
                tokenMapper.delete(Wrappers.<SysToken>lambdaQuery().in(SysToken::getUserId, userIds));
                userIds.forEach(loginUserService::evict);
            }
            List<Long> roleIds = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                            .select(SysRole::getId))
                    .stream()
                    .map(SysRole::getId)
                    .toList();
            if (!roleIds.isEmpty()) {
                roleMapper.deleteByIds(roleIds);
                roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().in(SysRoleMenu::getRoleId, roleIds));
            }
        } finally {
            TenantContext.setTenantId(originTenantId);
        }
    }

    /**
     * 初始化新租户的管理员角色、管理员账号与菜单授权。
     * <p>
     * 这些表都带 tenant_id，必须切到新租户的上下文再写入，写完恢复原上下文。
     */
    private void initTenant(Long tenantId, String username, String password) {
        Long originTenantId = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            SysRole role = new SysRole();
            role.setName(ADMIN_NAME);
            role.setCode(ADMIN_ROLE_CODE);
            role.setSort(0);
            role.setStatus(STATUS_ENABLE);
            role.setType(ROLE_TYPE_CUSTOM);
            role.setRemark("租户管理员，拥有本租户的全部菜单权限");
            roleMapper.insert(role);

            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setNickname(ADMIN_NAME);
            user.setStatus(STATUS_ENABLE);
            userMapper.insert(user);

            userRoleMapper.insert(SysUserRole.of(user.getId(), role.getId()));
            grantMenus(role.getId());
        } finally {
            TenantContext.setTenantId(originTenantId);
        }
    }

    /**
     * 授予除租户管理外的全部菜单
     */
    private void grantMenus(Long roleId) {
        List<SysMenu> menus = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .select(SysMenu::getId, SysMenu::getPermission, SysMenu::getPath));
        for (SysMenu menu : menus) {
            if (isPlatformMenu(menu)) {
                continue;
            }
            roleMenuMapper.insert(SysRoleMenu.of(roleId, menu.getId()));
        }
    }

    private boolean isPlatformMenu(SysMenu menu) {
        String permission = menu.getPermission();
        return (permission != null && permission.startsWith(PLATFORM_PERMISSION_PREFIX))
                || PLATFORM_MENU_PATH.equals(menu.getPath());
    }

    private void validateNameUnique(SysTenant tenant) {
        Long count = tenantMapper.selectCount(Wrappers.<SysTenant>lambdaQuery()
                .eq(SysTenant::getName, tenant.getName())
                .ne(tenant.getId() != null, SysTenant::getId, tenant.getId()));
        if (count > 0) {
            throw new BizException("租户名已存在");
        }
    }

}
