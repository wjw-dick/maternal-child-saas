package com.quantaida.mc.common;

import lombok.Getter;
import lombok.Setter;

public final class TenantContext {
    private static final ThreadLocal<Long> TENANT_ID = ThreadLocal.withInitial(() -> 0L);
    private static final ThreadLocal<String> TENANT_CODE = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId == null ? 0L : tenantId);
    }

    public static String getTenantCode() {
        return TENANT_CODE.get();
    }

    public static void setTenantCode(String code) {
        TENANT_CODE.set(code);
    }

    public static void clear() {
        TENANT_ID.remove();
        TENANT_CODE.remove();
    }
}
