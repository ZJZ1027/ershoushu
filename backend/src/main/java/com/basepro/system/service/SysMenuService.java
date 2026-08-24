package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.common.BizException;
import com.basepro.security.LoginUserService;
import com.basepro.system.dto.MenuQuery;
import com.basepro.system.entity.SysMenu;
import com.basepro.system.entity.SysRoleMenu;
import com.basepro.system.mapper.SysMenuMapper;
import com.basepro.system.mapper.SysRoleMenuMapper;
import com.basepro.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 菜单。sys_menu 是全平台共用的表，不做租户隔离，改动会影响所有租户的鉴权，
 * 因此增删改之后都要清空登录用户的权限缓存。
 */
@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final LoginUserService loginUserService;

    /**
     * 精简列表：角色授权树、上级菜单选择器共用。
     * 停用的菜单也一并返回，否则上级菜单选不到、授权树勾选状态会被误删。
     */
    public List<SysMenu> simpleList() {
        return menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .select(SysMenu::getId, SysMenu::getName, SysMenu::getParentId, SysMenu::getType)
                .orderByAsc(SysMenu::getParentId, SysMenu::getSort));
    }

    /**
     * 扁平列表，前端自行构建树
     */
    public List<SysMenu> list(MenuQuery query) {
        return menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), SysMenu::getName, query.getName())
                .eq(query.getStatus() != null, SysMenu::getStatus, query.getStatus())
                .orderByAsc(SysMenu::getParentId, SysMenu::getSort));
    }

    public SysMenu get(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }
        return menu;
    }

    public Long create(SysMenu menu) {
        menu.setId(null);
        if (menu.getParentId() == null) {
            menu.setParentId(SysMenu.ROOT_PARENT_ID);
        }
        validateParent(menu.getParentId(), null);
        validateNameUnique(menu);
        menuMapper.insert(menu);
        loginUserService.evictAll();
        return menu.getId();
    }

    public void update(SysMenu menu) {
        get(menu.getId());
        if (menu.getParentId() == null) {
            menu.setParentId(SysMenu.ROOT_PARENT_ID);
        }
        validateParent(menu.getParentId(), menu.getId());
        validateNameUnique(menu);
        menuMapper.updateById(menu);
        loginUserService.evictAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        get(id);
        Long children = menuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, id));
        if (children > 0) {
            throw new BizException("存在子菜单，无法删除");
        }
        menuMapper.deleteById(id);
        // 菜单是全局的，授权关系分散在各租户，清理时要忽略租户条件
        TenantContext.ignoreTenant(() -> {
            roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, id));
        });
        loginUserService.evictAll();
    }

    /**
     * 校验上级菜单：必须存在、必须是目录或菜单，且不能是自己或自己的子孙（否则菜单树成环）
     */
    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null || SysMenu.ROOT_PARENT_ID == parentId) {
            return;
        }
        if (parentId.equals(selfId)) {
            throw new BizException("上级菜单不能是自己");
        }
        SysMenu parent = menuMapper.selectById(parentId);
        if (parent == null) {
            throw new BizException("上级菜单不存在");
        }
        if (Integer.valueOf(SysMenu.TYPE_BUTTON).equals(parent.getType())) {
            throw new BizException("上级菜单只能是目录或菜单");
        }
        if (selfId == null) {
            return;
        }
        // 沿上级链往上找，遇到自己说明选中的是自己的子孙；visited 兜住脏数据造成的死循环
        Set<Long> visited = new HashSet<>();
        SysMenu node = parent;
        while (node != null && visited.add(node.getId())) {
            if (selfId.equals(node.getId())) {
                throw new BizException("上级菜单不能是自己的子菜单");
            }
            Long nextId = node.getParentId();
            node = nextId == null || SysMenu.ROOT_PARENT_ID == nextId ? null : menuMapper.selectById(nextId);
        }
    }

    private void validateNameUnique(SysMenu menu) {
        Long count = menuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery()
                .eq(SysMenu::getParentId, menu.getParentId())
                .eq(SysMenu::getName, menu.getName())
                .ne(menu.getId() != null, SysMenu::getId, menu.getId()));
        if (count > 0) {
            throw new BizException("同一上级菜单下已存在同名菜单");
        }
    }

}
