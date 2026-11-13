package com.quantaida.mc.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 业务编号生成：租户前缀 + 时间戳 + 序号。
 *
 * <p>启用 Redis 时使用 {@code INCR} 生成多实例唯一序号，key 为
 * {@code tenant:{id}:no:{prefix}:{yyyyMMdd}}；Redis 未启用时降级为 JVM 原子序号。</p>
 */
@Component
@RequiredArgsConstructor
public class NoGenerator {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RedisSupport redisSupport;
    private final AtomicInteger seq = new AtomicInteger(0);

    public String gen(String prefix) {
        LocalDateTime now = LocalDateTime.now();
        String key = "tenant:" + TenantContext.getTenantId() + ":no:" + prefix + ":" + now.format(DAY);
        Long redisSeq = redisSupport.increment(key, Duration.ofDays(2));
        long n = redisSeq != null ? redisSeq : seq.incrementAndGet();
        return prefix + now.format(TS) + String.format("%06d", Math.floorMod(n, 1_000_000));
    }
}
