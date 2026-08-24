package com.basepro.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 管理员重置指定用户的密码
 */
public record ResetPasswordReq(@NotNull(message = "用户编号不能为空") Long id,
                               @NotBlank(message = "新密码不能为空")
                               @Size(min = 4, max = 32, message = "新密码长度为 4-32 个字符")
                               String password) {
}
