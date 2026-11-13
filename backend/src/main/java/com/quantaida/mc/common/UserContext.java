package com.quantaida.mc.common;

public final class UserContext {
    public record LoginUser(Long userId, Long tenantId, String username, String userType, Long roleId, String roleCode, Long memberId) {
    }

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static Long userId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.userId();
    }

    public static Long memberId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.memberId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
