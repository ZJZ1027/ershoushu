package com.basepro.infra.controller;

import com.basepro.common.ExcelUtils;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.infra.dto.ConfigQuery;
import com.basepro.infra.entity.SysConfig;
import com.basepro.infra.service.SysConfigService;
import com.basepro.system.log.OperLog;
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

@Tag(name = "参数配置")
@RestController
@RequestMapping("/infra/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    @Operation(summary = "参数分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('infra:config:query')")
    public R<PageResult<SysConfig>> page(@Valid ConfigQuery query) {
        return R.ok(configService.page(query));
    }

    @Operation(summary = "参数详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('infra:config:query')")
    public R<SysConfig> get(@RequestParam("id") Long id) {
        return R.ok(configService.get(id));
    }

    @Operation(summary = "按键名查询参数值", description = "仅返回可见的参数，无需权限")
    @GetMapping("/get-value-by-key")
    public R<String> getValueByKey(@RequestParam("configKey") String configKey) {
        return R.ok(configService.getValueByKey(configKey));
    }

    @Operation(summary = "新增参数")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('infra:config:create')")
    @OperLog(module = "参数配置", name = "新增")
    public R<Long> create(@Valid @RequestBody SysConfig config) {
        return R.ok(configService.create(config));
    }

    @Operation(summary = "修改参数")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('infra:config:update')")
    @OperLog(module = "参数配置", name = "修改")
    public R<Void> update(@Valid @RequestBody SysConfig config) {
        configService.update(config);
        return R.ok();
    }

    @Operation(summary = "删除参数")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('infra:config:delete')")
    @OperLog(module = "参数配置", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        configService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除参数")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('infra:config:delete')")
    @OperLog(module = "参数配置", name = "批量删除")
    public R<Void> deleteList(@RequestParam("ids") List<Long> ids) {
        configService.delete(ids);
        return R.ok();
    }

    @Operation(summary = "导出参数")
    @GetMapping("/export-excel")
    @PreAuthorize("hasAuthority('infra:config:export')")
    public void exportExcel(@Valid ConfigQuery query, HttpServletResponse response) throws IOException {
        query.setPageSize(Integer.MAX_VALUE);
        List<SysConfig> list = configService.page(query).list();
        // 注意：单元格可能为 null，用 Arrays.asList 而不是 List.of
        ExcelUtils.export(response, "参数配置列表",
                List.of("参数编号", "参数分组", "参数名称", "参数键名", "参数键值", "参数类型", "是否可见", "备注", "创建时间"),
                list, config -> Arrays.asList(config.getId(), config.getCategory(), config.getName(),
                        config.getConfigKey(), config.getConfigValue(),
                        Integer.valueOf(SysConfig.TYPE_BUILT_IN).equals(config.getType()) ? "系统内置" : "自定义",
                        Boolean.TRUE.equals(config.getVisible()) ? "是" : "否",
                        config.getRemark(), config.getCreateTime()));
    }

}
