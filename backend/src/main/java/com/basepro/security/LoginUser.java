package com.basepro.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录用户。既作为 Spring Security 的 {@link UserDetails}（登录校验），
 * 也作为已认证请求的 principal（通过 {@link SecurityUtils} 读取）。
 */
public record LoginUser(Long userId,
                        String username,
                        @JsonIgnore String password,
                        String nickname,
                        Long tenantId,
                        Long deptId,
                        boolean enabled,
                        List<String> roles,
                        List<String> permissions) implements UserDetails {

    /**
     * 超级管理员角色标识：拥有全部权限
     */
    public static final String SUPER_ADMIN = "super_admin";

    public boolean isSuperAdmin() {
        return roles != null && roles.contains(SUPER_ADMIN);
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions == null ? List.of() : permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return enabled;
    }

}
