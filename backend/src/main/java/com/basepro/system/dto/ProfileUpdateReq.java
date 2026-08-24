package com.basepro.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 个人中心修改基本信息。只允许改这几项，其余字段（账号、部门、状态等）由管理员维护。
 * <p>
 * 各项均可为空：更换头像时前端只上送 avatar，为空的字段保持原值不变。
 */
public record ProfileUpdateReq(@Size(max = 30, message = "用户昵称长度不能超过 30 个字符") String nickname,
                               @Email(message = "邮箱格式不正确")
                               @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
                               String email,
                               @Size(max = 11, message = "手机号长度不能超过 11 位") String mobile,
                               Integer sex,
                               String avatar) {
}
