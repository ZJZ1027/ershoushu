package com.basepro.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 初始化并清理租户上下文。
 * <p>
 * 登录等未认证接口只能靠请求头 tenant-id 指定租户；已认证请求的租户以令牌里的 tenantId 为准，
 * 由 {@link com.basepro.security.JwtAuthConverter} 覆盖写入，避免前端伪造请求头越权。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String HEADER_TENANT_ID = "tenant-id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader(HEADER_TENANT_ID);
            if (header != null && !header.isBlank()) {
                try {
                    TenantContext.setTenantId(Long.parseLong(header.trim()));
                } catch (NumberFormatException ignored) {
                    // 请求头不合法时按默认租户处理
                }
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

}
