package com.basepro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Set;

/**
 * 项目自定义配置，对应 application.yml 中的 basepro.*
 */
@ConfigurationProperties(prefix = "basepro")
public record AppProperties(Jwt jwt, Tenant tenant, FileStore file) {

    public record Jwt(String secret, Duration accessTokenExpire, Duration refreshTokenExpire) {
    }

    /**
     * @param enabled      是否开启多租户（关闭后 SQL 不再追加 tenant_id 条件）
     * @param ignoreTables 无需租户隔离的表
     */
    public record Tenant(boolean enabled, Set<String> ignoreTables) {
    }

    /**
     * @param dir 本地文件存储目录
     */
    public record FileStore(String dir) {
    }

}
