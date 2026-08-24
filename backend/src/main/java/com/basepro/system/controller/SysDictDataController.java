package com.basepro.system.controller;

import com.basepro.common.ExcelUtils;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.DictDataQuery;
import com.basepro.system.entity.SysDictData;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysDictDataService;
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

@Tag(name = "字典数据")
@RestController
@RequestMapping("/system/dict-data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final SysDictDataService dictDataService;

    @Operation(summary = "字典数据分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public R<PageResult<SysDictData>> page(@Valid DictDataQuery query) {
        return R.ok(dictDataService.page(query));
    }

    @Operation(summary = "字典数据精简列表", description = "前端启动时全量拉取做本地字典缓存，无需权限")
    @GetMapping("/simple-list")
    public R<List<SysDictData>> simpleList() {
        return R.ok(dictDataService.simpleList());
    }

    @Operation(summary = "按字典类型查询字典数据")
    @GetMapping("/type")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public R<List<SysDictData>> listByType(@RequestParam("type") String type) {
        return R.ok(dictDataService.listByType(type));
    }

    @Operation(summary = "字典数据详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public R<SysDictData> get(@RequestParam("id") Long id) {
        return R.ok(dictDataService.get(id));
    }

    @Operation(summary = "新增字典数据")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:dict:create')")
    @OperLog(module = "字典数据", name = "新增")
    public R<Long> create(@Valid @RequestBody SysDictData dictData) {
        return R.ok(dictDataService.create(dictData));
    }

    @Operation(summary = "修改字典数据")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:dict:update')")
    @OperLog(module = "字典数据", name = "修改")
    public R<Void> update(@Valid @RequestBody SysDictData dictData) {
        dictDataService.update(dictData);
        return R.ok();
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @OperLog(module = "字典数据", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        dictDataService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除字典数据")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @OperLog(module = "字典数据", name = "批量删除")
    public R<Void> deleteList(@RequestParam("ids") List<Long> ids) {
        dictDataService.delete(ids);
        return R.ok();
    }

    @Operation(summary = "导出字典数据")
    @GetMapping("/export-excel")
    @PreAuthorize("hasAuthority('system:dict:export')")
    public void exportExcel(@Valid DictDataQuery query, HttpServletResponse response) throws IOException {
        query.setPageSize(Integer.MAX_VALUE);
        List<SysDictData> list = dictDataService.page(query).list();
        // 注意：单元格可能为 null，用 Arrays.asList 而不是 List.of
        ExcelUtils.export(response, "字典数据列表",
                List.of("字典编码", "字典类型", "字典标签", "字典键值", "显示顺序", "状态", "颜色类型", "CSS 样式", "备注", "创建时间"),
                list, dictData -> Arrays.asList(dictData.getId(), dictData.getDictType(), dictData.getLabel(),
                        dictData.getValue(), dictData.getSort(),
                        Integer.valueOf(0).equals(dictData.getStatus()) ? "正常" : "停用",
                        dictData.getColorType(), dictData.getCssClass(), dictData.getRemark(),
                        dictData.getCreateTime()));
    }

}
