# 宝塔部署与运维 | quantaida.cloud

> 对应任务：T79 / T80 / T82　要求：Nginx 仅 IPv4、HTTPS 通配符证书、进程守护自启、日志轮转、每日备份保留 7 份。

## 1. 服务器与域名规划

| 项 | 建议 |
| --- | --- |
| 系统 | CentOS 7+ / Ubuntu 20.04+ / Debian 11+，4 核 8 G 起 |
| 磁盘 | 系统盘 50 G + 数据盘 200 G（对象存储可另挂） |
| 软件 | 宝塔 8.x、Nginx 1.24+、MySQL 8.0、Redis 7、RabbitMQ 3.12、JDK 17 |
| 端口 | 80/443 对公网；8080 仅本机；3306/6379/5672 不对公网 |

DNS 记录（均为 A 记录，指向同一台 IPv4）：

```
quantaida.cloud          -> 服务器 IPv4
www.quantaida.cloud      -> 服务器 IPv4
admin.quantaida.cloud    -> 服务器 IPv4
app.quantaida.cloud      -> 服务器 IPv4
api.quantaida.cloud      -> 服务器 IPv4
*.quantaida.cloud        -> 服务器 IPv4   # 租户子域名，如 demo.quantaida.cloud
```

> 不建议使用 IPv6 记录；Nginx 配置中也只写 IPv4 `listen`。

## 2. 目录规划

```
/www/wwwroot/mc-saas/
├── backend/                 jar、配置、data/storage
├── admin/dist/              管理后台静态资源
├── site/dist/               PC 官网静态资源
├── uniapp/dist/build/h5/    H5 静态资源
├── deploy/                  nginx.conf、systemd、备份脚本
├── docs/                    文档
└── .env                     生产环境变量（权限 600）
/www/wwwlogs/mc-saas/        systemd 输出日志
/www/backup/mc-saas/         MySQL 备份（保留 7 份）
```

## 3. 安装基础软件（宝塔）

1. 宝塔「软件商店」安装：Nginx、MySQL 8.0、Redis、RabbitMQ、Java 项目管理器（可选）。
2. 安装 JDK 17：

```bash
# 确认版本
java -version
# 如未安装（Debian/Ubuntu）
apt install -y openjdk-17-jdk
# CentOS
yum install -y java-17-openjdk
```

3. MySQL 8 建议参数（8 G 内存）：

```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
max_connections=300
innodb_buffer_pool_size=2G
innodb_log_file_size=512M
slow_query_log=1
long_query_time=2
```

4. Redis：

```conf
bind 127.0.0.1
protected-mode yes
requirepass <强密码>
appendonly yes
maxmemory 1gb
maxmemory-policy allkeys-lru
```

5. RabbitMQ：

```bash
rabbitmq-plugins enable rabbitmq_management
rabbitmqctl add_user mc_saas '<强密码>'
rabbitmqctl set_permissions -p / mc_saas ".*" ".*" ".*"
rabbitmqctl set_user_tags mc_saas administrator
```

> 管理端口 15672 不要对公网开放；如需访问，用 SSH 隧道。

## 4. 初始化数据库

```bash
mysql -uroot -p -e "
CREATE DATABASE maternal_child DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'mc_saas'@'127.0.0.1' IDENTIFIED BY '<强密码>';
GRANT ALL PRIVILEGES ON maternal_child.* TO 'mc_saas'@'127.0.0.1';
FLUSH PRIVILEGES;"

mysql -uroot -p maternal_child < /www/wwwroot/mc-saas/backend/src/main/resources/db/schema.sql
mysql -uroot -p maternal_child < /www/wwwroot/mc-saas/backend/src/main/resources/db/data.sql
mysql -uroot -p maternal_child < /www/wwwroot/mc-saas/backend/src/main/resources/db/mysql-index.sql
```

验证：

```bash
mysql -umc_saas -p -h127.0.0.1 maternal_child -e "SHOW TABLES;" | wc -l   # 应为 78（含表头）
mysql -umc_saas -p -h127.0.0.1 maternal_child -e "SELECT tenant_code,name,status FROM platform_tenant;"
```

## 5. 环境变量

```bash
cd /www/wwwroot/mc-saas
cp deploy/.env.example .env
chmod 600 .env
vi .env
```

必须修改：

- `DB_PASSWORD`、`REDIS_PASSWORD`、`RABBIT_PASSWORD`
- `JWT_SECRET`（≥32 位随机）
- `APP_CRYPTO_KEY`（32 字节；变更前评估历史密文）
- `SERVER_ADDRESS=127.0.0.1`（后端只监听本机）
- `STORAGE_ROOT=/www/wwwroot/mc-saas/backend/data/storage`

生产建议 `REDIS_ENABLED=true`、`MQ_ENABLED=true`、`JOB_ENABLED=true`。

## 6. 构建与上传

### 6.1 后端

```bash
cd /www/wwwroot/mc-saas/backend
mvn -q -DskipTests package
ls -lh target/maternal-child-saas.jar
```

### 6.2 管理后台

```bash
cd /www/wwwroot/mc-saas/admin
npm ci
npm run build
# 产物 admin/dist
```

### 6.3 PC 官网

```bash
cd /www/wwwroot/mc-saas/site
npm ci
npm run build
# 产物 site/dist
```

### 6.4 uni-app H5

```bash
cd /www/wwwroot/mc-saas/uniapp
npm ci
npm run build:h5
# 产物 uniapp/dist/build/h5
```

> 生产 API 地址通过构建环境变量或 Nginx 同域 `/api` 代理解决；如使用独立 API 域名，在 `uniapp/src/api/request.js` 与 `admin` 的 `VITE_API_BASE` 中配置。

## 7. systemd 守护

```bash
cp /www/wwwroot/mc-saas/deploy/mc-saas.service /etc/systemd/system/
mkdir -p /www/wwwlogs/mc-saas /www/wwwroot/mc-saas/backend/data/storage
chown -R www:www /www/wwwroot/mc-saas /www/wwwlogs/mc-saas /www/backup/mc-saas
systemctl daemon-reload
systemctl enable --now mc-saas
systemctl status mc-saas --no-pager
```

查看日志：

```bash
journalctl -u mc-saas -f
tail -f /www/wwwlogs/mc-saas/app.err
tail -f /www/wwwroot/mc-saas/backend/logs/app.log
```

> 若宝塔「Java 项目管理器」已托管进程，二选一，避免重复启动。推荐 systemd，便于 `enable` 自启与统一日志。

## 8. 申请通配符证书

宝塔 → SSL → Let's Encrypt：

1. 选择「通配符证书」。
2. 域名填 `*.quantaida.cloud` 与 `quantaida.cloud`。
3. 验证方式选 DNS（阿里云/腾讯云 API）。
4. 申请成功后确认证书路径：

```
/www/server/panel/vhost/cert/quantaida.cloud/fullchain.pem
/www/server/panel/vhost/cert/quantaida.cloud/privkey.pem
```

5. 开启自动续期；续期后 `nginx -t && nginx -s reload`。

## 9. Nginx 配置

1. 宝塔新建站点 `quantaida.cloud`（不要自动创建数据库/FTP）。
2. 将 `deploy/nginx.conf` 内容替换站点配置文件。
3. 确认证书路径与实际一致。
4. 测试并重载：

```bash
nginx -t
nginx -s reload
```

### 9.1 仅 IPv4 校验（强制）

```bash
ss -lntp | grep -E ':80|:443'
```

期望只看到 `0.0.0.0:80`、`0.0.0.0:443` 或 `*:443`，**不应出现 `[::]:80` / `[::]:443`**。

检查配置中不得出现：

```bash
grep -n "\[::\]" /www/server/panel/vhost/nginx/quantaida.cloud.conf
# 无输出为正确
```

如宝塔自动生成 IPv6 监听，删除对应 `listen [::]:...` 行后 reload。

### 9.2 路由说明

| 域名/路径 | 去向 |
| --- | --- |
| `quantaida.cloud` | `site/dist` PC 官网 |
| `admin.quantaida.cloud` | `admin/dist` 管理后台 |
| `app.quantaida.cloud`、`*.quantaida.cloud` | `uniapp/dist/build/h5` |
| `api.quantaida.cloud` | 全部反代后端 |
| `/api/` | 反代 `127.0.0.1:8080` |
| `/files/` | 本地对象存储 `backend/data/storage` |

## 10. 防火墙与安全

- 安全组仅开放 22、80、443；3306/6379/5672/15672/8080 仅内网或本机。
- `SERVER_ADDRESS=127.0.0.1`，确保 8080 不直接暴露。
- 修改默认 SSH 端口、禁用密码登录、配置 Fail2Ban。
- `.env` 权限 600，属主 `www`。
- 定期更新 JDK/Nginx/MySQL/Redis/RabbitMQ 安全补丁。
- 管理后台建议加 IP 白名单或宝塔「网站防火墙」。
- 开启 Nginx 访问日志与慢请求日志，保留 ≥30 天。

## 11. 备份与恢复

```bash
# 手动备份
/bin/bash /www/wwwroot/mc-saas/deploy/backup.sh

# 查看
ls -lht /www/backup/mc-saas | head

# 恢复（二次确认，自动生成恢复前快照）
/bin/bash /www/wwwroot/mc-saas/deploy/restore.sh /www/backup/mc-saas/maternal_child_20261113_023000.sql.gz
```

宝塔计划任务：每天 02:30 执行 `backup.sh`，保留 7 份。详见 `deploy/bt-tasks.md`。

## 12. 升级与回滚

### 升级

```bash
cd /www/wwwroot/mc-saas
/bin/bash deploy/backup.sh
# 上传新代码/构建产物
cd backend && mvn -q -DskipTests package
cd ../admin && npm ci && npm run build
cd ../site && npm ci && npm run build
cd ../uniapp && npm ci && npm run build:h5
# 执行增量 SQL（如有）
systemctl restart mc-saas
nginx -t && nginx -s reload
curl -fsS https://api.quantaida.cloud/api/public/config
```

### 回滚

1. 保留上一版本 `backend/target/maternal-child-saas.jar` 与前端 `dist` 目录（按日期归档）。
2. 恢复上一版 jar / 前端目录。
3. 若数据库有变更，使用升级前备份恢复。
4. `systemctl restart mc-saas`，验证登录、看板、下单、预约。

## 13. 压测

```bash
# 安装依赖
yum install -y httpd-tools jq   # 或 apt install -y apache2-utils jq

BASE=https://api.quantaida.cloud TENANT=demo bash deploy/bench.sh
```

关注：QPS、P95/P99、错误率、Hikari 连接池、Redis 命中率、RabbitMQ 积压。详见 `docs/performance.md`。

## 14. 常见问题

| 现象 | 排查 |
| --- | --- |
| 502 Bad Gateway | `systemctl status mc-saas`；8080 是否监听；`.env` 数据库密码 |
| 登录 40301 | 租户 `status` 为 SUSPENDED/EXPIRED；平台续期 |
| 页面刷新 404 | Nginx `try_files ... /index.html` 是否配置 |
| 中文乱码 | MySQL `utf8mb4`、连接串 `characterEncoding=utf8mb4`、`file.encoding=UTF-8` |
| 证书不生效 | `nginx -t`；证书路径；是否仍有 HTTP 缓存；DNS 是否解析到本机 |
| 出现 IPv6 监听 | 删除 `listen [::]:...`；检查宝塔是否自动生成 |
| 文件上传 50010 | 当前仅 LOCAL 可写；云端驱动需接入 SDK 与凭证 |
| 队列积压 | RabbitMQ 管理台查看消费者；日志 `[MQ-*]`；死信 `mc.dead.queue` |
| 定时任务不执行 | `JOB_ENABLED=true`；cron 表达式；多实例 Redis 锁 |
| 跨租户 403 | 正常安全拦截；检查 `X-Tenant-Code`/子域名与 JWT 是否一致 |

## 15. 上线检查

- [ ] 域名解析与通配符证书生效
- [ ] Nginx 仅 IPv4 监听
- [ ] 后端 8080 仅本机监听
- [ ] MySQL/Redis/RabbitMQ 不对公网开放
- [ ] `.env` 密钥已更换、权限 600
- [ ] 数据库 77 张表 + 索引 + 演示/正式租户
- [ ] systemd `enable --now` 且重启服务器后自动恢复
- [ ] 每日备份任务生效，保留 7 份，恢复演练通过
- [ ] 平台/租户登录、看板、下单、预约、上传、导出回归通过
- [ ] 日志轮转与磁盘告警配置
