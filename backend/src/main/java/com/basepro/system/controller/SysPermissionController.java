package com.basepro.system.controller;

import com.basepro.common.R;
import com.basepro.system.dto.AssignRoleMenuReq;
import com.basepro.system.dto.AssignUserRoleReq;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Tag(name = "授权")
@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService permissionService;

    @Operation(summary = "查询角色拥有的菜单编号")
    @GetMapping("/list-role-menus")
    @PreAuthorize("hasAuthority('system:permission:assign-role-menu')")
    public R<Set<Long>> listRoleMenus(@RequestParam("roleId") Long roleId) {
        return R.ok(permissionService.listRoleMenus(roleId));
    }

    @Operation(summary = "赋予角色菜单权限", description = "全量覆盖，未提交的菜单会被收回")
    @PostMapping("/assign-role-menu")
    @PreAuthorize("hasAuthority('system:permission:assign-role-menu')")
    @OperLog(module = "授权", name = "角色菜单授权")
    public R<Void> assignRoleMenu(@Valid @RequestBody AssignRoleMenuReq request) {
        permissionService.assignRoleMenu(request);
        return R.ok();
    }

    @Operation(summary = "查询用户拥有的角色编号")
    @GetMapping("/list-user-roles")
    @PreAuthorize("hasAuthority('system:permission:assign-user-role')")
    public R<Set<Long>> listUserRoles(@RequestParam("userId") Long userId) {
        return R.ok(permissionService.listUserRoles(userId));
    }

    @Operation(summary = "赋予用户角色", description = "全量覆盖，未提交的角色会被收回")
    @PostMapping("/assign-user-role")
    @PreAuthorize("hasAuthority('system:permission:assign-user-role')")
    @OperLog(module = "授权", name = "用户角色授权")
    public R<Void> assignUserRole(@Valid @RequestBody AssignUserRoleReq request) {
        permissionService.assignUserRole(request);
        return R.ok();
    }

}
