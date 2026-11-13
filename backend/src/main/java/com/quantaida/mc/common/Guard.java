package com.quantaida.mc.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流 + 防重。
 *
 * <p>优先使用 Redis（key 统一带 {@code tenant:{id}:} 前缀），Redis 未启用或不可用时
 * 自动降级为 JVM 单机实现，保证本地演示与生产部署使用同一套调用代码。</p>
 */
@Component
@RequiredArgsConstructor
public class Guard {
    private record Bucket(long windowStart, AtomicInteger count) {
    }

    private final RedisSupport redisSupport;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Long> idemKeys = new ConcurrentHashMap<>();

    public void rateLimit(String key, int limit, int seconds) {
        String tenantKey = tenantKey("rl:" + key + ":" + (System.currentTimeMillis() / 1000 / Math.max(seconds, 1)));
        Long count = redisSupport.increment(tenantKey, Duration.ofSeconds(seconds + 2L));
        if (count != null) {
            if (count > limit) {
                throw new BizException(429, "操作过于频繁，请稍后再试");
            }
            return;
        }
        localRateLimit(key, limit, seconds);
    }

    public void idempotent(String key) {
        String tenantKey = tenantKey("idem:" + key);
        Boolean first = redisSupport.setIfAbsent(tenantKey, "1", Duration.ofMinutes(10));
        if (first != null) {
            if (!first) {
                throw new BizException(429, "请勿重复提交");
            }
            return;
        }
        localIdempotent(key);
    }

    private String tenantKey(String key) {
        return "tenant:" + TenantContext.getTenantId() + ":" + key;
    }

    private void localRateLimit(String key, int limit, int seconds) {
        long now = System.currentTimeMillis();
        Bucket bucket = buckets.compute(key, (k, v) -> {
            if (v == null || now - v.windowStart() > seconds * 1000L) {
                return new Bucket(now, new AtomicInteger(1));
            }
            v.count().incrementAndGet();
            return v;
        });
        if (bucket.count().get() > limit) {
            throw new BizException(429, "操作过于频繁，请稍后再试");
        }
    }

    private void localIdempotent(String key) {
        long now = System.currentTimeMillis();
        idemKeys.entrySet().removeIf(e -> now - e.getValue() > 600_000L);
        if (idemKeys.putIfAbsent(key, now) != null) {
            throw new BizException(429, "请勿重复提交");
        }
    }
}
