package com.basepro.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 个人中心修改密码
 */
public record UpdatePasswordReq(@NotBlank(message = "原密码不能为空") String oldPassword,
                                @NotBlank(message = "新密码不能为空")
                                @Size(min = 4, max = 32, message = "新密码长度为 4-32 个字符")
                                String newPassword) {
}
