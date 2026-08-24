package com.basepro.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * 角色的菜单授权请求。全量覆盖语义：menuIds 为空表示收回该角色的全部菜单。
 */
public record AssignRoleMenuReq(
        @Schema(description = "角色编号")
        @NotNull(message = "角色编号不能为空")
        Long roleId,

        @Schema(description = "菜单编号集合")
        Set<Long> menuIds) {
}
