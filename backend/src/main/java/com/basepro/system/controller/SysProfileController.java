package com.basepro.system.controller;

import com.basepro.common.R;
import com.basepro.system.dto.ProfileUpdateReq;
import com.basepro.system.dto.ProfileVO;
import com.basepro.system.dto.UpdatePasswordReq;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心。只要登录就能访问，操作对象固定为当前登录用户，因此不加权限注解。
 */
@Tag(name = "个人中心")
@RestController
@RequestMapping("/system/user/profile")
@RequiredArgsConstructor
public class SysProfileController {

    private final SysProfileService profileService;

    @Operation(summary = "获取个人信息", description = "含所属部门、角色、岗位")
    @GetMapping("/get")
    public R<ProfileVO> get() {
        return R.ok(profileService.get());
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/update")
    @OperLog(module = "个人中心", name = "修改个人信息")
    public R<Void> update(@Valid @RequestBody ProfileUpdateReq request) {
        profileService.update(request);
        return R.ok();
    }

    @Operation(summary = "修改个人密码")
    @PutMapping("/update-password")
    @OperLog(module = "个人中心", name = "修改密码", saveParams = false)
    public R<Void> updatePassword(@Valid @RequestBody UpdatePasswordReq request) {
        profileService.updatePassword(request);
        return R.ok();
    }

}
