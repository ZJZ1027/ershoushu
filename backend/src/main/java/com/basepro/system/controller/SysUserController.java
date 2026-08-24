package com.basepro.system.controller;

import com.basepro.common.ExcelUtils;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.ResetPasswordReq;
import com.basepro.system.dto.UpdateStatusReq;
import com.basepro.system.dto.UserImportResult;
import com.basepro.system.dto.UserQuery;
import com.basepro.system.entity.SysUser;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysUserService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Tag(name = "用户")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "用户分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R<PageResult<SysUser>> page(@Valid UserQuery query) {
        return R.ok(userService.page(query));
    }

    @Operation(summary = "用户精简列表", description = "下拉选择用，无需权限")
    @GetMapping("/simple-list")
    public R<List<SysUser>> simpleList() {
        return R.ok(userService.simpleList());
    }

    @Operation(summary = "用户详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R<SysUser> get(@RequestParam("id") Long id) {
        return R.ok(userService.get(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:user:create')")
    @OperLog(module = "用户", name = "新增", saveParams = false)
    public R<Long> create(@Valid @RequestBody SysUser user) {
        return R.ok(userService.create(user));
    }

    @Operation(summary = "修改用户", description = "不支持修改密码")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:user:update')")
    @OperLog(module = "用户", name = "修改")
    public R<Void> update(@Valid @RequestBody SysUser user) {
        userService.update(user);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @OperLog(module = "用户", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        userService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @OperLog(module = "用户", name = "批量删除")
    public R<Void> deleteList(@RequestParam("ids") List<Long> ids) {
        userService.delete(ids);
        return R.ok();
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/update-password")
    @PreAuthorize("hasAuthority('system:user:update-password')")
    @OperLog(module = "用户", name = "重置密码", saveParams = false)
    public R<Void> updatePassword(@Valid @RequestBody ResetPasswordReq request) {
        userService.updatePassword(request.id(), request.password());
        return R.ok();
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/update-status")
    @PreAuthorize("hasAuthority('system:user:update')")
    @OperLog(module = "用户", name = "修改状态")
    public R<Void> updateStatus(@Valid @RequestBody UpdateStatusReq request) {
        userService.updateStatus(request.id(), request.status());
        return R.ok();
    }

    @Operation(summary = "导出用户")
    @GetMapping("/export-excel")
    @PreAuthorize("hasAuthority('system:user:export')")
    public void exportExcel(@Valid UserQuery query, HttpServletResponse response) throws IOException {
        query.setPageSize(Integer.MAX_VALUE);
        List<SysUser> list = userService.page(query).list();
        // 注意：单元格可能为 null，用 Arrays.asList 而不是 List.of
        ExcelUtils.export(response, "用户列表",
                List.of("编号", "用户账号", "用户昵称", "所属部门", "手机号", "邮箱", "性别", "状态",
                        "最后登录 IP", "最后登录时间", "创建时间"),
                list, user -> Arrays.asList(user.getId(), user.getUsername(), user.getNickname(),
                        user.getDeptName(), user.getMobile(), user.getEmail(), sexLabel(user.getSex()),
                        Integer.valueOf(0).equals(user.getStatus()) ? "正常" : "停用",
                        user.getLoginIp(), user.getLoginDate(), user.getCreateTime()));
    }

    @Operation(summary = "下载用户导入模板")
    @GetMapping("/get-import-template")
    @PreAuthorize("hasAuthority('system:user:import')")
    public void getImportTemplate(HttpServletResponse response) throws IOException {
        List<Object> sample = Arrays.asList("basepro", "脚手架用户", 100L, "basepro@example.com",
                "13800000000", "示例数据，导入前请删除本行");
        ExcelUtils.export(response, "用户导入模板",
                List.of("用户账号", "用户昵称", "部门编号", "邮箱", "手机号", "备注"),
                List.of(sample), row -> row);
    }

    @Operation(summary = "导入用户", description = "新增的用户使用默认初始密码，导入后请提醒用户自行修改")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('system:user:import')")
    @OperLog(module = "用户", name = "导入")
    public R<UserImportResult> importExcel(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "updateSupport", defaultValue = "false")
                                          boolean updateSupport) throws IOException {
        return R.ok(userService.importUsers(file, updateSupport));
    }

    private String sexLabel(Integer sex) {
        if (sex == null) {
            return null;
        }
        return switch (sex.intValue()) {
            case 1 -> "男";
            case 2 -> "女";
            default -> null;
        };
    }

}
