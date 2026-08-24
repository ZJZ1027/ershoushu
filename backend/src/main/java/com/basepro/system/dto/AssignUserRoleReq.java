package com.basepro.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * 用户的角色授权请求。全量覆盖语义：roleIds 为空表示收回该用户的全部角色。
 */
public record AssignUserRoleReq(
        @Schema(description = "用户编号")
        @NotNull(message = "用户编号不能为空")
        Long userId,

        @Schema(description = "角色编号集合")
        Set<Long> roleIds) {
}
