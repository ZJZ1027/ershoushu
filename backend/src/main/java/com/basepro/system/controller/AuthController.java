package com.basepro.system.controller;

import com.basepro.common.R;
import com.basepro.system.dto.LoginReq;
import com.basepro.system.dto.PermissionInfoVO;
import com.basepro.system.dto.TokenVO;
import com.basepro.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证")
@RestController
@RequestMapping("/system/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<TokenVO> login(@Valid @RequestBody LoginReq request) {
        return R.ok(authService.login(request));
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh-token")
    public R<TokenVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return R.ok(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @Operation(summary = "获取登录用户的权限信息")
    @GetMapping("/get-permission-info")
    public R<PermissionInfoVO> getPermissionInfo() {
        return R.ok(authService.getPermissionInfo());
    }

}
