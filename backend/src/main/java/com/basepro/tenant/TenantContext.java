package com.basepro.tenant;

import java.util.function.Supplier;

/**
 * 当前租户上下文。请求进入时由 {@link TenantContextFilter} 写入，请求结束时清理。
 */
public final class TenantContext {

    /**
     * 平台默认租户，也是超级管理员所属租户
     */
    public static final long DEFAULT_TENANT_ID = 1L;

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IGNORE = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Long getTenantId() {
        Long tenantId = TENANT_ID.get();
        return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static boolean isIgnored() {
        return Boolean.TRUE.equals(IGNORE.get());
    }

    /**
     * 在忽略租户隔离的上下文中执行，用于平台级操作（如租户管理）。
     */
    public static <T> T ignoreTenant(Supplier<T> supplier) {
        Boolean origin = IGNORE.get();
        IGNORE.set(Boolean.TRUE);
        try {
            return supplier.get();
        } finally {
            IGNORE.set(origin);
        }
    }

    public static void ignoreTenant(Runnable runnable) {
        ignoreTenant(() -> {
            runnable.run();
            return null;
        });
    }

    public static void clear() {
        TENANT_ID.remove();
        IGNORE.remove();
    }

}
