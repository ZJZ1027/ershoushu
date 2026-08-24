package com.basepro.system.controller;

import com.basepro.common.ExcelUtils;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.TenantQuery;
import com.basepro.system.entity.SysTenant;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysTenantService;
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

@Tag(name = "租户")
@RestController
@RequestMapping("/system/tenant")
@RequiredArgsConstructor
public class SysTenantController {

    private final SysTenantService tenantService;

    @Operation(summary = "租户分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:tenant:query')")
    public R<PageResult<SysTenant>> page(@Valid TenantQuery query) {
        return R.ok(tenantService.page(query));
    }

    @Operation(summary = "租户精简列表", description = "切换租户的下拉选择用，无需权限")
    @GetMapping("/simple-list")
    public R<List<SysTenant>> simpleList() {
        return R.ok(tenantService.simpleList());
    }

    @Operation(summary = "租户详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('system:tenant:query')")
    public R<SysTenant> get(@RequestParam("id") Long id) {
        return R.ok(tenantService.get(id));
    }

    @Operation(summary = "新增租户", description = "同时初始化该租户的管理员角色与管理员账号")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:tenant:create')")
    @OperLog(module = "租户", name = "新增")
    public R<Long> create(@Valid @RequestBody SysTenant tenant) {
        return R.ok(tenantService.create(tenant));
    }

    @Operation(summary = "修改租户")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:tenant:update')")
    @OperLog(module = "租户", name = "修改")
    public R<Void> update(@Valid @RequestBody SysTenant tenant) {
        tenantService.update(tenant);
        return R.ok();
    }

    @Operation(summary = "删除租户")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('system:tenant:delete')")
    @OperLog(module = "租户", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        tenantService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除租户")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('system:tenant:delete')")
    @OperLog(module = "租户", name = "批量删除")
    public R<Void> deleteList(@RequestParam("ids") List<Long> ids) {
        tenantService.delete(ids);
        return R.ok();
    }

    @Operation(summary = "导出租户")
    @GetMapping("/export-excel")
    @PreAuthorize("hasAuthority('system:tenant:export')")
    public void exportExcel(@Valid TenantQuery query, HttpServletResponse response) throws IOException {
        query.setPageSize(Integer.MAX_VALUE);
        List<SysTenant> list = tenantService.page(query).list();
        // 注意：单元格可能为 null，用 Arrays.asList 而不是 List.of
        ExcelUtils.export(response, "租户列表", List.of("编号", "租户名", "联系人", "联系手机", "状态", "绑定域名",
                        "账号额度", "过期时间", "创建时间"),
                list, tenant -> Arrays.asList(tenant.getId(), tenant.getName(), tenant.getContactName(),
                        tenant.getContactMobile(),
                        Integer.valueOf(0).equals(tenant.getStatus()) ? "正常" : "停用",
                        tenant.getDomain(), tenant.getAccountCount(), tenant.getExpireTime(),
                        tenant.getCreateTime()));
    }

}
