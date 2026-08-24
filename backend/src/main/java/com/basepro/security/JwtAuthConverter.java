package com.basepro.security;

import com.basepro.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

/**
 * 把访问令牌换成本次请求的登录用户：
 * 用户信息与权限实时读库（带本地缓存），因此禁用账号、改授权能及时生效；
 * 租户也以令牌为准，避免前端伪造 tenant-id 请求头越权。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final LoginUserService loginUserService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long userId;
        try {
            userId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new InvalidBearerTokenException("令牌格式不正确");
        }
        LoginUser loginUser = loginUserService.loadById(userId);
        if (loginUser == null) {
            throw new InvalidBearerTokenException("账号不存在");
        }
        if (!loginUser.isEnabled()) {
            throw new InvalidBearerTokenException("账号已被停用");
        }
        TenantContext.setTenantId(loginUser.tenantId());
        return new UsernamePasswordAuthenticationToken(loginUser, jwt, loginUser.getAuthorities());
    }

}
