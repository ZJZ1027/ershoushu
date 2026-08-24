package com.basepro.system.controller;

import com.basepro.common.R;
import com.basepro.system.dto.MenuQuery;
import com.basepro.system.entity.SysMenu;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "菜单")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @Operation(summary = "菜单精简列表", description = "角色授权树、上级菜单选择器用，无需权限")
    @GetMapping("/simple-list")
    public R<List<SysMenu>> simpleList() {
        return R.ok(menuService.simpleList());
    }

    @Operation(summary = "菜单列表", description = "不分页，返回扁平列表，由前端构建树")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R<List<SysMenu>> list(@Valid MenuQuery query) {
        return R.ok(menuService.list(query));
    }

    @Operation(summary = "菜单详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R<SysMenu> get(@RequestParam("id") Long id) {
        return R.ok(menuService.get(id));
    }

    @Operation(summary = "新增菜单")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:menu:create')")
    @OperLog(module = "菜单", name = "新增")
    public R<Long> create(@Valid @RequestBody SysMenu menu) {
        return R.ok(menuService.create(menu));
    }

    @Operation(summary = "修改菜单")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:menu:update')")
    @OperLog(module = "菜单", name = "修改")
    public R<Void> update(@Valid @RequestBody SysMenu menu) {
        menuService.update(menu);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @OperLog(module = "菜单", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        menuService.delete(id);
        return R.ok();
    }

}
