package com.quantaida.mc.common;

/**
 * 租户级定时任务扩展点。
 *
 * <p>实现类只需关注单租户业务逻辑，调度器负责遍历租户、设置 TenantContext、
 * 获取分布式锁并隔离单个租户的异常。</p>
 */
public interface TenantJob {
    String name();

    void run(Long tenantId);
}
