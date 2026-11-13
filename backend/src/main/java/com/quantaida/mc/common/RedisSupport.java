package com.quantaida.mc.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis 可选能力封装。
 *
 * <p>本地 H2 演示环境默认不启动 Redis，任何 Redis 调用失败都会自动降级为单机实现，
 * 并在 30 秒内短路，避免每个请求都等待连接超时。生产环境设置
 * {@code app.redis.enabled=true} 后启用分布式限流/防重/编号/锁。</p>
 */
@Slf4j
@Component
public class RedisSupport {
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final ObjectProvider<StringRedisTemplate> templateProvider;
    private final boolean enabled;
    private final AtomicLong downUntil = new AtomicLong(0);

    public RedisSupport(ObjectProvider<StringRedisTemplate> templateProvider,
                        @org.springframework.beans.factory.annotation.Value("${app.redis.enabled:false}") boolean enabled) {
        this.templateProvider = templateProvider;
        this.enabled = enabled;
    }

    public boolean available() {
        return enabled && System.currentTimeMillis() >= downUntil.get() && templateProvider.getIfAvailable() != null;
    }

    private StringRedisTemplate template() {
        return templateProvider.getObject();
    }

    private void markDown(Exception e) {
        downUntil.set(System.currentTimeMillis() + 30_000L);
        log.warn("Redis 不可用，已降级单机实现 30 秒: {}", e.getMessage());
    }

    /**
     * 自增并设置过期时间，失败返回 null（调用方降级）。
     */
    public Long increment(String key, Duration ttl) {
        if (!available()) {
            return null;
        }
        try {
            Long value = template().opsForValue().increment(key);
            if (value != null && value == 1L) {
                template().expire(key, ttl);
            }
            return value;
        } catch (Exception e) {
            markDown(e);
            return null;
        }
    }

    /**
     * SETNX + TTL，失败返回 null（调用方降级）。
     */
    public Boolean setIfAbsent(String key, String value, Duration ttl) {
        if (!available()) {
            return null;
        }
        try {
            return template().opsForValue().setIfAbsent(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            markDown(e);
            return null;
        }
    }

    public boolean tryLock(String key, String owner, Duration ttl) {
        Boolean ok = setIfAbsent(key, owner, ttl);
        // Redis 不可用时由单机任务直接执行；多实例生产环境必须保证 Redis 可用。
        return ok == null || ok;
    }

    public void unlock(String key, String owner) {
        if (!available()) {
            return;
        }
        try {
            template().execute(UNLOCK_SCRIPT, List.of(key), owner);
        } catch (Exception e) {
            markDown(e);
        }
    }
}
