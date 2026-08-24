package com.basepro.system.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 修改用户状态：0 正常、1 停用
 */
public record UpdateStatusReq(@NotNull(message = "用户编号不能为空") Long id,
                              @NotNull(message = "状态不能为空") Integer status) {
}
