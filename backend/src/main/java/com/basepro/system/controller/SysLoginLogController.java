package com.basepro.system.controller;

import com.basepro.common.ExcelUtils;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.LoginLogQuery;
import com.basepro.system.entity.SysLoginLog;
import com.basepro.system.service.SysLoginLogService;
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

@Tag(name = "登录日志")
@RestController
@RequestMapping("/system/login-log")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogService loginLogService;

    @Operation(summary = "登录日志分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:login-log:query')")
    public R<PageResult<SysLoginLog>> page(@Valid LoginLogQuery query) {
        return R.ok(loginLogService.page(query));
    }

    @Operation(summary = "导出登录日志")
    @GetMapping("/export-excel")
    @PreAuthorize("hasAuthority('system:login-log:export')")
    public void exportExcel(@Valid LoginLogQuery query, HttpServletResponse response) throws IOException {
        query.setPageSize(Integer.MAX_VALUE);
        List<SysLoginLog> list = loginLogService.page(query).list();
        // 注意：单元格可能为 null，用 Arrays.asList 而不是 List.of
        ExcelUtils.export(response, "登录日志", List.of("编号", "日志类型", "用户账号", "登录结果", "登录 IP",
                        "登录客户端", "登录时间"),
                list, log -> Arrays.asList(log.getId(), logTypeText(log.getLogType()), log.getUsername(),
                        resultText(log.getResult()), log.getUserIp(), log.getUserAgent(), log.getCreateTime()));
    }

    private static String logTypeText(Integer logType) {
        if (logType == null) {
            return null;
        }
        return logType == SysLoginLog.TYPE_LOGOUT ? "登出" : "登录";
    }

    private static String resultText(Integer result) {
        if (result == null) {
            return null;
        }
        return switch (result) {
            case SysLoginLog.RESULT_SUCCESS -> "成功";
            case SysLoginLog.RESULT_BAD_CREDENTIALS -> "账号或密码不正确";
            case SysLoginLog.RESULT_DISABLED -> "账号被停用";
            default -> "失败";
        };
    }

}
