package com.basepro.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 当前请求的读取工具。
 */
public final class WebUtils {

    private static final String UNKNOWN = "unknown";

    private WebUtils() {
    }

    public static HttpServletRequest getRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    /**
     * 获取客户端 IP，兼容 Nginx 等反向代理
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        for (String header : new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"}) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !UNKNOWN.equalsIgnoreCase(value)) {
                int index = value.indexOf(',');
                return index > 0 ? value.substring(0, index).trim() : value.trim();
            }
        }
        return request.getRemoteAddr();
    }

    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        return userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 512));
    }

}
