# 性能与压测报告 | 母婴亲子 SaaS

> 对应任务：T81　测试日期：2026-11-13　环境：本地 H2 MySQL 模式（非生产基准）

## 1. 测试目标

| 指标 | 目标（生产 4C8G） | 本地参考 |
| --- | --- | --- |
| 公开列表 QPS | ≥ 500 | ≥ 1,000 |
| 租户看板 QPS | ≥ 300 | ≥ 800 |
| 列表查询 P95 | ≤ 200 ms | ≤ 50 ms |
| 下单/预约 P95 | ≤ 300 ms | ≤ 100 ms |
| 错误率 | < 0.1% | 0 |
| 缓存命中率 | ≥ 80%（热点参数/字典） | 待生产 Redis 验证 |
| 队列消费 | 无积压、无死信 | 待生产 RabbitMQ 验证 |

> 本地结果仅用于回归对比，不能直接代表生产容量；生产需在真实 MySQL/Redis/RabbitMQ 上复测。

## 2. 测试工具

- `deploy/bench-node.mjs`：Node 18+，无第三方依赖，支持并发、P95/P99、错误率。
- `deploy/bench.sh`：生产环境使用 `wrk` 或 `ab`，支持登录 token。
- 监控：Hikari 连接池、JVM GC、Redis 命中率、RabbitMQ 队列深度、Nginx 5xx。

## 3. 本地实测结果

命令：

```bash
BASE=http://127.0.0.1:18080 TENANT=demo REQUESTS=100 CONCURRENCY=10 node deploy/bench-node.mjs
```

结果：

```
BASE=http://127.0.0.1:18080 TENANT=demo
公开服务   req=100 c=10 qps=1698.9 avg=5.7ms  p95=19.7ms p99=22.2ms err=0
租户看板   req=100 c=10 qps=1970.4 avg=5.0ms  p95=28.2ms p99=29.1ms err=0
会员列表   req=100 c=10 qps=2009.8 avg=4.9ms  p95=17.3ms p99=19.3ms err=0
订单列表   req=100 c=10 qps=3190.0 avg=3.1ms  p95=4.8ms  p99=6.0ms  err=0
预约列表   req=100 c=10 qps=2952.5 avg=3.2ms  p95=7.0ms  p99=7.4ms  err=0
RESULT=PASS
```

结论：

- 5 个关键读接口全部 0 错误，P95 均低于 30 ms（H2 内存库）。
- 租户隔离插件未造成明显额外开销。
- 本地未启用 Redis/RabbitMQ，走的是单机降级路径；生产需开启后复测。

## 4. 生产压测步骤

1. 准备独立压测租户与数据（≥ 1 万会员、10 万订单、5 万预约）。
2. 开启 Redis、RabbitMQ、`JOB_ENABLED=false`（避免压测期间定时任务干扰）。
3. 使用 `deploy/bench.sh`：
   ```bash
   BASE=https://api.quantaida.cloud TENANT=perf \
     USERNAME=perf PASSWORD='***' \
     DURATION=60s CONNECTIONS=100 THREADS=8 \
     bash deploy/bench.sh
   ```
4. 分阶段提升并发：50 → 100 → 200 → 500，记录 QPS/P95/P99/错误率。
5. 观察 MySQL `SHOW PROCESSLIST`、慢查询、Hikari active/idle、Redis 命中率、RabbitMQ ready/unacked。
6. 写接口单独压测：下单、预约、支付、报单，验证防重与库存一致性。
7. 输出报告并归档，作为容量规划依据。

## 5. 写接口压测要点

| 接口 | 注意事项 |
| --- | --- |
| 下单 | 每请求使用唯一 `idempotencyKey`；验证库存不超卖 |
| 预约 | 每请求唯一 key；验证时段容量与排班 `booked` |
| 支付 | 同一订单重复支付应幂等；避免重复扣库存 |
| 退款 | 仅已支付订单；验证库存回滚与流水 |
| 报单 | 渠道商 + 业务键防重；验证额度扣减 |
| 开放 API | 每请求唯一 nonce；验证配额计数与限流 |

## 6. Redis 验证清单

- [ ] `REDIS_ENABLED=true` 后应用启动无异常。
- [ ] 限流 key 形如 `tenant:{id}:rl:*`，无裸 key。
- [ ] 防重 key 形如 `tenant:{id}:idem:*`，10 分钟过期。
- [ ] 编号 key 形如 `tenant:{id}:no:*`，跨实例不重复。
- [ ] 定时任务锁 `platform:job:{name}:{tenantId}:{date}` 生效。
- [ ] Redis 宕机后 30 秒内降级单机，业务不中断。

## 7. RabbitMQ 验证清单

- [ ] `MQ_ENABLED=true` 后 exchange/queue 自动声明。
- [ ] 下单/预约事件进入 `mc.biz.queue`，手动 ACK。
- [ ] 通知事件进入 `mc.notify.queue`。
- [ ] 延迟消息经 TTL + DLX 进入通知队列。
- [ ] 消费异常进入 `mc.dead.queue`，不阻塞后续消息。
- [ ] 消息头包含 `tenantId`，日志可按租户检索。
- [ ] RabbitMQ 不可用时 EventBus 降级日志，主业务不失败。

## 8. 调优建议

| 瓶颈 | 建议 |
| --- | --- |
| MySQL CPU 高 | 补复合索引、慢查询优化、读写分离 |
| 连接池等待 | 调整 `maximum-pool-size`（20~50），缩短事务 |
| 列表慢 | 强制分页、限制 `size`、避免 `SELECT *` 大字段 |
| 图片慢 | 对象存储 + CDN，开启浏览器缓存 |
| 队列积压 | 增加消费者并发、批量处理、死信告警 |
| GC 频繁 | 调整堆大小、G1 参数、减少大对象 |
| Redis 命中低 | 热点参数/字典缓存、合理 TTL、避免缓存穿透 |

## 9. 回归结论

- 本地只读关键接口：**PASS**。
- 写接口功能回归：见 `checklist.md` 与后端测试。
- Redis/RabbitMQ 实机验证：需在生产或预发环境执行，按 §6/§7 勾选。
