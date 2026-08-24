package com.basepro.system.controller;

import com.basepro.common.R;
import com.basepro.system.dto.DeptQuery;
import com.basepro.system.entity.SysDept;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysDeptService;
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

@Tag(name = "部门")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @Operation(summary = "部门精简列表", description = "下拉选择用，无需权限")
    @GetMapping("/simple-list")
    public R<List<SysDept>> simpleList() {
        return R.ok(deptService.simpleList());
    }

    @Operation(summary = "部门列表", description = "不分页，返回扁平列表由前端拼树")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:dept:query')")
    public R<List<SysDept>> list(DeptQuery query) {
        return R.ok(deptService.list(query));
    }

    @Operation(summary = "部门详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('system:dept:query')")
    public R<SysDept> get(@RequestParam("id") Long id) {
        return R.ok(deptService.get(id));
    }

    @Operation(summary = "新增部门")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:dept:create')")
    @OperLog(module = "部门", name = "新增")
    public R<Long> create(@Valid @RequestBody SysDept dept) {
        return R.ok(deptService.create(dept));
    }

    @Operation(summary = "修改部门")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:dept:update')")
    @OperLog(module = "部门", name = "修改")
    public R<Void> update(@Valid @RequestBody SysDept dept) {
        deptService.update(dept);
        return R.ok();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('system:dept:delete')")
    @OperLog(module = "部门", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        deptService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除部门")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('system:dept:delete')")
    @OperLog(module = "部门", name = "批量删除")
    public R<Void> deleteList(@RequestParam("ids") List<Long> ids) {
        deptService.delete(ids);
        return R.ok();
    }

}
