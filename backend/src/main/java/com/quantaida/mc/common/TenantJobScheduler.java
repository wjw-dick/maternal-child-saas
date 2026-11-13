package com.quantaida.mc.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 按租户分片的定时任务调度器。
 *
 * <p>每天遍历启用租户，逐个设置 {@link TenantContext} 执行任务；某个租户失败只记录日志，
 * 不影响其他租户。Redis 可用时使用日期维度分布式锁，避免多实例重复执行。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantJobScheduler {
    private final List<TenantJob> jobs;
    private final PlatformTenantMapper tenantMapper;
    private final RedisSupport redisSupport;

    @Value("${app.job.enabled:false}")
    private boolean enabled;

    @Scheduled(cron = "${app.job.cron:0 0 3 * * ?}")
    public void runDaily() {
        if (!enabled || jobs.isEmpty()) {
            return;
        }
        List<PlatformTenant> tenants = tenantMapper.selectList(new LambdaQueryWrapper<PlatformTenant>()
                .eq(PlatformTenant::getStatus, "ACTIVE"));
        log.info("[JOB] 开始租户分片任务，租户数={} 任务数={}", tenants.size(), jobs.size());
        for (PlatformTenant tenant : tenants) {
            for (TenantJob job : jobs) {
                String owner = UUID.randomUUID().toString();
                String lockKey = "platform:job:" + job.name() + ":" + tenant.getId() + ":" + LocalDate.now();
                if (!redisSupport.tryLock(lockKey, owner, Duration.ofHours(6))) {
                    log.info("[JOB] 其他实例已执行 {}/{}，跳过", job.name(), tenant.getTenantCode());
                    continue;
                }
                try {
                    TenantContext.setTenantId(tenant.getId());
                    TenantContext.setTenantCode(tenant.getTenantCode());
                    job.run(tenant.getId());
                } catch (Exception e) {
                    log.error("[JOB] 租户 {} 任务 {} 失败: {}", tenant.getTenantCode(), job.name(), e.getMessage(), e);
                } finally {
                    TenantContext.clear();
                    UserContext.clear();
                }
            }
        }
        log.info("[JOB] 租户分片任务结束");
    }
}
