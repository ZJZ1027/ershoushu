package com.basepro.system.controller;

import com.basepro.common.ExcelUtils;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.OperLogQuery;
import com.basepro.system.entity.SysOperLog;
import com.basepro.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/system/operate-log")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService operLogService;

    @Operation(summary = "操作日志分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:operate-log:query')")
    public R<PageResult<SysOperLog>> page(@Valid OperLogQuery query) {
        return R.ok(operLogService.page(query));
    }

    @Operation(summary = "导出操作日志")
    @GetMapping("/export-excel")
    @PreAuthorize("hasAuthority('system:operate-log:export')")
    public void exportExcel(@Valid OperLogQuery query, HttpServletResponse response) throws IOException {
        query.setPageSize(Integer.MAX_VALUE);
        List<SysOperLog> list = operLogService.page(query).list();
        // 注意：单元格可能为 null，用 Arrays.asList 而不是 List.of
        ExcelUtils.export(response, "操作日志", List.of("编号", "模块", "操作", "操作人", "请求方法", "请求地址",
                        "执行时长（毫秒）", "结果", "结果提示", "操作 IP", "操作时间"),
                list, log -> Arrays.asList(log.getId(), log.getModule(), log.getName(), log.getUsername(),
                        log.getRequestMethod(), log.getRequestUrl(), log.getDuration(),
                        SysOperLogService.isSuccess(log.getResultCode()) ? "成功" : "失败",
                        log.getResultMsg(), log.getUserIp(), log.getCreateTime()));
    }

}
