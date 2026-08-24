package com.basepro.system.dto;

/**
 * 登录令牌。字段名与前端 TokenType 一致。
 *
 * @param expiresTime 访问令牌过期时间（毫秒时间戳）
 */
public record TokenVO(String accessToken,
                      String refreshToken,
                      Long userId,
                      Long tenantId,
                      Long expiresTime) {
}
