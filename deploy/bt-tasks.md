# 宝塔计划任务与运维任务

## 1. 每日数据库备份

宝塔面板 → 计划任务 → 添加任务：

| 项 | 值 |
| --- | --- |
| 任务类型 | Shell 脚本 |
| 任务名称 | mc-saas 数据库备份 |
| 执行周期 | 每天 02:30 |
| 脚本内容 | `/bin/bash /www/wwwroot/mc-saas/deploy/backup.sh >> /www/backup/mc-saas/cron.log 2>&1` |

验证：

```bash
ls -lht /www/backup/mc-saas | head
gzip -t /www/backup/mc-saas/maternal_child_*.sql.gz
```

保留策略：`BACKUP_KEEP=7`，脚本只清理 `BACKUP_DIR` 下匹配 `maternal_child_*.sql.gz` 的历史文件。

## 2. 证书续期

宝塔 → SSL → Let's Encrypt → 通配符证书 `*.quantaida.cloud`：

- 使用 DNS 验证（阿里云/腾讯云 DNS API）。
- 自动续期任务保持开启。
- 续期后执行 `nginx -t && nginx -s reload`。
- 监控到期时间，至少提前 15 天告警。

> 证书文件默认路径：`/www/server/panel/vhost/cert/quantaida.cloud/fullchain.pem`、`privkey.pem`。如面板路径不同，修改 `deploy/nginx.conf`。

## 3. 日志轮转

后端日志由 logback 按 50 MB 滚动、保留 30 天（`application.yml`）。
Nginx 日志由宝塔默认轮转；systemd 输出：

```bash
mkdir -p /www/wwwlogs/mc-saas
```

建议每周清理 30 天前的 `app.out`/`app.err`：

```bash
find /www/wwwlogs/mc-saas -type f -name '*.out' -mtime +30 -delete
find /www/wwwlogs/mc-saas -type f -name '*.err' -mtime +30 -delete
```

## 4. 健康检查

宝塔 → 计划任务 → 每 5 分钟：

```bash
curl -fsS --max-time 5 https://api.quantaida.cloud/api/public/config >/dev/null || \
  systemctl restart mc-saas
```

建议同时接入宝塔「网站监控」或外部拨测。

## 5. 恢复演练（每季度）

1. 在测试库执行 `deploy/restore.sh`。
2. 核对 77 张表、租户 `demo`、订单/会员数量。
3. 启动测试后端，验证登录、看板、订单列表。
4. 记录演练时间、备份文件、结果，归档到运维台账。

## 6. 发布流程

```bash
cd /www/wwwroot/mc-saas
# 1) 备份
/bin/bash deploy/backup.sh
# 2) 拉取/上传新版本（禁止 git push）
# 3) 后端构建
cd backend && mvn -q -DskipTests package
# 4) 前端构建
cd ../admin && npm ci && npm run build
cd ../site && npm ci && npm run build
cd ../uniapp && npm ci && npm run build:h5
# 5) 执行增量 SQL（如有）
# 6) 重启
systemctl restart mc-saas
systemctl status mc-saas --no-pager
nginx -t && nginx -s reload
# 7) 回归
curl -fsS https://api.quantaida.cloud/api/public/config
```
