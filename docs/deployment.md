# 生产上线记录

## 1. 访问入口

| 端 | 域名 | 内容 |
| --- | --- | --- |
| 主站 / 引导页 | https://maternal-child.quantaida.cloud | 引导页 + `/api/`、`/files/` 反代 |
| 用户端 H5 | https://maternal-child-h5.quantaida.cloud | uni-app H5 构建产物 |
| 运营管理后台 | https://maternal-child-admin.quantaida.cloud | Vue3 + Vite 构建产物 |
| 品牌官网 Portal | https://maternal-child-portal.quantaida.cloud | `site/` Vue3 品牌官网（双语 / SEO） |

## 2. 服务器与目录

- 服务器：`172.16.29.20:40122`（宝塔面板 `https://172.16.29.20:7878`）
- 主站目录：`/www/wwwroot/maternal-child.quantaida.cloud`
- H5 目录：`/www/wwwroot/maternal-child-h5.quantaida.cloud`
- 后台目录：`/www/wwwroot/maternal-child-admin.quantaida.cloud`
- 官网目录：`/www/wwwroot/maternal-child-portal.quantaida.cloud`
- 后端目录：`/home/app/maternal-child.quantaida.cloud`（`app.jar`、`.env`、`service.sh`、`logs/`、`data/storage/`）
- 后端端口：`18081`
- JDK：`/www/server/java/jdk-17.0.8`

站点目录统一 `www:www`、755；`.user.ini` 为宝塔保护文件，`chown` 报“不允许的操作”属正常现象。

## 3. 数据库

- 库名：`maternal_child_saas`（utf8mb4）
- 应用账号：`mc_app@localhost`
- 已导入 `backend/src/main/resources/db/schema.sql` 与 `data.sql`，共 78 张表
- `application-mysql.yml` 中的 JDBC URL 使用 `characterEncoding=utf8mb4`，MySQL Connector/J 8.3 不接受该编码名。上线时**未修改业务代码**，通过 `.env` 的 `SPRING_DATASOURCE_URL` 覆盖为 `characterEncoding=utf8`，数据库与表仍为 utf8mb4。

## 4. 运行配置

`.env` 位于 `/home/app/maternal-child.quantaida.cloud/.env`，不进入 Git：

- `SPRING_PROFILES_ACTIVE=mysql`
- `SERVER_PORT=18081`
- `SPRING_DATASOURCE_URL`：指向本机 MySQL，使用 utf8 连接编码
- `REDIS_ENABLED=true`：启用本机 Redis（限流、防重、编号、锁）
- `MQ_ENABLED=false`、`JOB_ENABLED=false`：RabbitMQ 位于旧网段、定时任务依赖分片配置，暂不启用
- `STORAGE_ROOT=/home/app/maternal-child.quantaida.cloud/data/storage`：本地存储，非 LOCAL 驱动仍需接入云存储 SDK
- `SSO_MOCK=false`

## 5. 服务管理

```bash
cd /home/app/maternal-child.quantaida.cloud
./service.sh start|stop|restart|status
./service.sh enable   # 已注册 systemd 开机自启：maternal-child.service
```

日志：`/home/app/maternal-child.quantaida.cloud/logs/app.log`

## 6. Nginx

- 配置文件：`/www/server/panel/vhost/nginx/maternal-child*.quantaida.cloud.conf`
- 三个站点均为自包含配置，监听 80/443，`/api/` 与 `/files/` 反代 `127.0.0.1:18081`
- Portal 构建时注入 `VITE_TENANT_CODE=demo`，否则公开接口缺少 `X-Tenant-Code`，服务/门店列表为空
- SSL 使用 `*.quantaida.cloud` 通配符证书（复制到各站点 cert 目录），TLS 1.2/1.3
- 仓库内配置样例：`deploy/nginx/`

## 7. 上线验证

| 检查 | 结果 |
| --- | --- |
| 后端启动 | `Started McApplication in 5.878 seconds`，18081 LISTEN |
| 数据库 | Hikari 连接成功，78 张表，租户 1 |
| 后端登录 | `POST /api/tenant/auth/login` 200，返回租户管理员 token |
| 主站 | HTTP/HTTPS 200，返回引导页标题“母婴亲子 SaaS · 多租户数字化平台” |
| H5 | 200，`nurse/admin123` 登录进入“一线服务工作台”，今日预约 1、排班 `2026-11-13` |
| 管理后台 | 200，`demo/admin/admin123` 登录进入经营工作台，家庭会员 2、今日预约 1、累计实收 127.00 |
| 品牌官网 | 200，首页、服务 3 条、门店 2 家正常加载，独立 `-portal` 站点与 API 反代通过 |
| 公网 API | `https://maternal-child.quantaida.cloud/api/tenant/auth/login` 200 |
| 开机自启 | `systemctl is-enabled maternal-child` = enabled |

## 8. 已知边界

- 真实微信/支付宝支付仍为 MOCK，未接真实收银台
- 对象存储仍为 LOCAL，S3/OSS/COS/MinIO SDK 未接入
- RabbitMQ 异步事件、租户分片定时任务未启用
- Flutter 独立 App 未构建商店包，未接厂商推送
