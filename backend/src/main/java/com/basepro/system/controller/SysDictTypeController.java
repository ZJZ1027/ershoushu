package com.basepro.system.controller;

import com.basepro.common.ExcelUtils;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.DictTypeQuery;
import com.basepro.system.entity.SysDictType;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysDictTypeService;
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

@Tag(name = "字典类型")
@RestController
@RequestMapping("/system/dict-type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final SysDictTypeService dictTypeService;

    @Operation(summary = "字典类型分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public R<PageResult<SysDictType>> page(@Valid DictTypeQuery query) {
        return R.ok(dictTypeService.page(query));
    }

    @Operation(summary = "字典类型精简列表", description = "下拉选择用，无需权限")
    @GetMapping("/simple-list")
    public R<List<SysDictType>> simpleList() {
        return R.ok(dictTypeService.simpleList());
    }

    @Operation(summary = "字典类型详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public R<SysDictType> get(@RequestParam("id") Long id) {
        return R.ok(dictTypeService.get(id));
    }

    @Operation(summary = "新增字典类型")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:dict:create')")
    @OperLog(module = "字典类型", name = "新增")
    public R<Long> create(@Valid @RequestBody SysDictType dictType) {
        return R.ok(dictTypeService.create(dictType));
    }

    @Operation(summary = "修改字典类型")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:dict:update')")
    @OperLog(module = "字典类型", name = "修改")
    public R<Void> update(@Valid @RequestBody SysDictType dictType) {
        dictTypeService.update(dictType);
        return R.ok();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @OperLog(module = "字典类型", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        dictTypeService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除字典类型")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @OperLog(module = "字典类型", name = "批量删除")
    public R<Void> deleteList(@RequestParam("ids") List<Long> ids) {
        dictTypeService.delete(ids);
        return R.ok();
    }

    @Operation(summary = "导出字典类型")
    @GetMapping("/export-excel")
    @PreAuthorize("hasAuthority('system:dict:export')")
    public void exportExcel(@Valid DictTypeQuery query, HttpServletResponse response) throws IOException {
        query.setPageSize(Integer.MAX_VALUE);
        List<SysDictType> list = dictTypeService.page(query).list();
        // 注意：单元格可能为 null，用 Arrays.asList 而不是 List.of
        ExcelUtils.export(response, "字典类型列表", List.of("字典编号", "字典名称", "字典类型", "状态", "备注", "创建时间"),
                list, dictType -> Arrays.asList(dictType.getId(), dictType.getName(), dictType.getType(),
                        Integer.valueOf(0).equals(dictType.getStatus()) ? "正常" : "停用",
                        dictType.getRemark(), dictType.getCreateTime()));
    }

}
