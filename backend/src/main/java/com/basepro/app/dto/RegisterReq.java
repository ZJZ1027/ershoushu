package com.basepro.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterReq(@NotBlank(message = "账号不能为空") @Size(max = 30) String username,
                          @NotBlank(message = "密码不能为空") @Size(min = 6, max = 32) String password,
                          @NotBlank(message = "昵称不能为空") @Size(max = 30) String nickname,
                          String mobile,
                          String campus) {
}
