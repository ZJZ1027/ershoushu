package com.basepro.system.controller;

import com.basepro.common.ExcelUtils;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.RoleQuery;
import com.basepro.system.entity.SysRole;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Tag(name = "角色")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "角色分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R<PageResult<SysRole>> page(@Valid RoleQuery query) {
        return R.ok(roleService.page(query));
    }

    @Operation(summary = "角色精简列表", description = "下拉选择用，无需权限")
    @GetMapping("/simple-list")
    public R<List<SysRole>> simpleList() {
        return R.ok(roleService.simpleList());
    }

    @Operation(summary = "角色详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R<SysRole> get(@RequestParam("id") Long id) {
        return R.ok(roleService.get(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:role:create')")
    @OperLog(module = "角色", name = "新增")
    public R<Long> create(@Valid @RequestBody SysRole role) {
        return R.ok(roleService.create(role));
    }

    @Operation(summary = "修改角色")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:role:update')")
    @OperLog(module = "角色", name = "修改")
    public R<Void> update(@Valid @RequestBody SysRole role) {
        roleService.update(role);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @OperLog(module = "角色", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        roleService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除角色")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @OperLog(module = "角色", name = "批量删除")
    public R<Void> deleteList(@RequestParam("ids") List<Long> ids) {
        roleService.delete(ids);
        return R.ok();
    }

    @Operation(summary = "导出角色")
    @GetMapping("/export-excel")
    @PreAuthorize("hasAuthority('system:role:export')")
    public void exportExcel(@Valid RoleQuery query, HttpServletResponse response) throws IOException {
        query.setPageSize(Integer.MAX_VALUE);
        List<SysRole> list = roleService.page(query).list();
        // 注意：单元格可能为 null，用 Arrays.asList 而不是 List.of
        ExcelUtils.export(response, "角色列表",
                List.of("编号", "角色名称", "角色标识", "角色类型", "显示顺序", "状态", "备注", "创建时间"),
                list, role -> Arrays.asList(role.getId(), role.getName(), role.getCode(),
                        Integer.valueOf(SysRole.TYPE_BUILT_IN).equals(role.getType()) ? "内置" : "自定义",
                        role.getSort(),
                        Integer.valueOf(0).equals(role.getStatus()) ? "正常" : "停用",
                        role.getRemark(), role.getCreateTime()));
    }

}
