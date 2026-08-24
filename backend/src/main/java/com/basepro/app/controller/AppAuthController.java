package com.basepro.app.controller;

import com.basepro.app.dto.AppProfileUpdateReq;
import com.basepro.app.dto.AppProfileVO;
import com.basepro.app.dto.RegisterReq;
import com.basepro.app.service.AppAuthService;
import com.basepro.common.R;
import com.basepro.system.dto.LoginReq;
import com.basepro.system.dto.TokenVO;
import com.basepro.system.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AppAuthController {

    private final AppAuthService appAuthService;
    private final AuthService authService;

    @PostMapping("/register")
    public R<TokenVO> register(@Valid @RequestBody RegisterReq req) {
        return R.ok(appAuthService.register(req));
    }

    @PostMapping("/login")
    public R<TokenVO> login(@Valid @RequestBody LoginReq req) {
        return R.ok(appAuthService.login(req));
    }

    @PostMapping("/refresh-token")
    public R<TokenVO> refresh(@RequestParam String refreshToken) {
        return R.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @GetMapping("/profile")
    public R<AppProfileVO> profile() {
        return R.ok(appAuthService.profile());
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@Valid @RequestBody AppProfileUpdateReq req) {
        appAuthService.updateProfile(req);
        return R.ok();
    }

}
