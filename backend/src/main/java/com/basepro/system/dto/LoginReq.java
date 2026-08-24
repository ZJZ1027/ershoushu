package com.basepro.system.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求。
 *
 * @param tenantName 租户名，多租户下用于定位租户；不传则使用默认租户
 */
public record LoginReq(@NotBlank(message = "账号不能为空") String username,
                       @NotBlank(message = "密码不能为空") String password,
                       String tenantName) {
}
