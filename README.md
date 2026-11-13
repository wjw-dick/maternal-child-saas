# 母婴亲子 数字化多端平台（多租户 SaaS）

面向母婴护理、月子中心、亲子服务连锁的一体化多租户 SaaS 平台。共享数据库 + `tenant_id` 行级隔离，覆盖平台运营端、租户管理后台、PC 品牌官网、微信公众号、小程序/H5、业务一线端、员工端、供应商端、渠道商端、Flutter App 与开放 API。

## 目录结构

```
maternal-child-saas/
├── backend/                 Spring Boot 3 单体后端（Java 17 + MyBatis-Plus）
│   ├── src/main/java/       租户底座、RBAC、业务域、微信、开放 API
│   └── src/main/resources/  application*.yml、db/schema.sql、db/data.sql
├── admin/                   Vue3 + Element Plus 平台/租户管理后台
├── site/                    Vue3 + Vite PC 品牌官网（双语 / SEO / hreflang）
├── uniapp/                  uni-app 小程序 + H5 + App 壳（会员/员工/一线/供应商/渠道商）
├── flutter_app/             Flutter iOS + Android App
├── docs/                    架构、数据库、API、部署、用户手册
├── deploy/                  Nginx、systemd、备份恢复、压测脚本
├── PRO.md                   产品需求与总体设计
├── tasks.md                 T01~T82 开发任务
├── checklist.md             上线验收清单
└── CLAUDE.md                开发行为准则
```

## 技术栈

| 层次 | 选型 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2、MyBatis-Plus 3.5（租户插件）、Spring Security Crypto、JJWT、Hutool、POI |
| 数据 | MySQL 8（生产）/ H2 MySQL 模式（本地演示）、Redis、RabbitMQ |
| 管理后台 | Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router |
| 官网 | Vue 3、Vite、原生 CSS、双语 i18n、SEO/hreflang |
| 多端 | uni-app 3（微信小程序 / H5 / App 壳） |
| App | Flutter 3（iOS + Android 单仓双端） |
| 部署 | 宝塔面板 + Nginx（仅 IPv4）+ 通配符证书 `*.quantaida.cloud` + systemd |

## 快速开始（本地演示）

### 1. 启动后端（H2 内存库，零外部依赖）

```bash
cd backend
mvn spring-boot:run
# 默认 8080；如被占用：
# PowerShell: $env:SERVER_PORT='18080'; mvn spring-boot:run
```

首次启动自动执行 `db/schema.sql`（77 张表）与 `db/data.sql`（演示租户、角色、菜单、商品/服务/资讯等）。默认租户 `demo`，默认账号密码统一 `admin123`：

| 端 | 账号 | 说明 |
| --- | --- | --- |
| 平台端 | `admin` | 平台超管 |
| 租户后台 | `admin` | 租户管理员（登录时租户填 `demo`） |
| 一线护理师 | `nurse` | 接单、签到、护理记录 |
| 员工/前台 | `clerk` | 核销、开卡、订单 |
| 财务 | `finance` | 报表、账单 |
| 供应商 | `supplier01` | 供货单、对账 |
| 渠道商 | `dist01` | 报单、佣金 |

### 2. 管理后台

```bash
cd admin
npm install
npm run dev
# 如后端不是 8080：
# PowerShell: $env:VITE_API_TARGET='http://127.0.0.1:18080'; npm run dev
```

访问 `http://127.0.0.1:5173`，平台账号与租户账号使用不同登录入口/路由。

### 3. PC 官网

```bash
cd site
npm install
npm run dev
# 默认 http://127.0.0.1:5175
```

### 4. uni-app（小程序 / H5）

```bash
cd uniapp
npm install
npm run dev:h5
npm run build:mp-weixin   # 产物 dist/build/mp-weixin，用微信开发者工具导入
```

如后端端口非 8080，设置 `VITE_API_TARGET`。小程序端需在 `manifest.json` 填入真实 AppID，并在微信公众平台配置合法域名。

### 5. Flutter App

```bash
cd flutter_app
flutter pub get
flutter run
flutter analyze
flutter test
```

后端地址在 `lib/core/api.dart`（或环境配置）中调整。Android 模拟器访问宿主机使用 `10.0.2.2`。

## 数据库与配置

- 本地 H2：默认 profile `h2`，每次启动重建，无需安装数据库。
- MySQL 8：

```bash
mysql -uroot -p -e "CREATE DATABASE maternal_child DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p maternal_child < backend/src/main/resources/db/schema.sql
mysql -uroot -p maternal_child < backend/src/main/resources/db/data.sql
mysql -uroot -p maternal_child < backend/src/main/resources/db/mysql-index.sql
```

启动时指定 `SPRING_PROFILES_ACTIVE=mysql`，并配置 `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`。

关键环境变量见 `.env.example` / `deploy/.env.example`：`JWT_SECRET`、`APP_CRYPTO_KEY`、`REDIS_ENABLED`、`MQ_ENABLED`、`JOB_ENABLED`、`STORAGE_ROOT`。

> 生产环境必须修改 `JWT_SECRET` 与 `APP_CRYPTO_KEY`；`APP_CRYPTO_KEY` 变更后历史密文无法解密，请先评估。

## 多租户与安全

- MyBatis-Plus 租户插件自动注入 `tenant_id`；`platform_*`、`sys_dict_*` 等表白名单显式忽略。
- 平台端 `/api/platform/*` 与租户端 `/api/tenant/*` 物理分离；JWT 中携带 `userType`，拦截器校验端与租户状态。
- 跨租户 ID 访问返回 `403` 并写安全审计；租户停用后接口直接 `403`。
- 手机号/证件号加密存储、列表脱敏；`phone_hash` 与 `unionid` 去重。
- Redis 可选：限流、防重、编号、定时任务锁；未启用时自动降级单机实现。
- RabbitMQ 可选：订单/预约/通知异步事件、TTL + 死信延迟队列、手动 ACK、失败重试。
- 定时任务按租户分片，单租户异常不影响其他租户。

## 文档

- [架构设计](docs/architecture.md)
- [数据库设计](docs/database.md)
- [API 接口](docs/api.md)
- [宝塔部署与运维](docs/deploy-baota.md)
- [性能与压测](docs/performance.md)
- [用户手册目录](docs/user-manual/README.md)
- [验收清单](checklist.md)
- [验收执行报告（2026-11-13）](docs/acceptance-report.md)

## 本轮验收状态（2026-11-13）

- 后端 `mvn test`：**9/9 通过**（基础设施 4 + 租户隔离 5）。
- 管理后台 / PC 官网 / uni-app H5：构建通过；Flutter `analyze` 0 issue、`test` 全部通过。
- 多租户：新租户开通 0.22s 可用；跨租户资源 `403 + 审计`；租户列表互不串数据；平台审计可按 tenantId 筛选。
- SSO：启用后 OIDC 授权链接正确、沙箱登录签发 token；停用后立即拒绝并降级账号密码。
- 公众号：11 子页齐备，数据统计 4 图支持 7/15/30 天切换。
- 员工端：新增独立工作台（今日预约/核销、开卡储值、库存入库/出库/盘点、订单完成、排班、简报、修改密码），员工账号 `clerk/admin123` 可实测。
- 一线端：新增服务工单页（GPS 签到、现场拍照、宝宝体征/护理记录、客户电子签名、完成服务）、收入与工时、个人中心（档案/公告/协议/改密）；护理师账号 `nurse/admin123` 可实测。
- 供应商端：供货单发货（PENDING→SHIPPED）与签收、对账单/结算状态、资质证照上传与审核状态、平台公告、修改密码；账号 `supplier01/admin123`。
- 渠道商端：授信额度、报单提交、佣金流水、我的订单、下级渠道、结算记录、公告、修改密码；账号 `dist01/admin123`。
- 积分：后台新增积分规则/积分流水页，支持调整积分并落流水。
- App（Flutter）：新增订单页（支付、售后申请），个人中心支持修改密码与服务协议。
- 未完成/外部依赖：S3/OSS/COS/MinIO SDK、跨门店库存调拨、MySQL/Redis/RabbitMQ 真机联调、宝塔部署与压测、真实微信/支付/短信/推送凭证。详见验收报告第九章。

## 验收与测试

```bash
# 后端隔离测试（5 项）
cd backend && mvn test

# 管理后台构建
cd admin && npm run build

# 官网构建
cd site && npm run build

# uni-app H5 构建
cd uniapp && npm run build:h5

# Flutter 静态检查与测试
cd flutter_app && flutter analyze && flutter test
```

## 交付清单

1. 后端源码 + 77 张表初始化脚本 + 演示数据 + MySQL 索引脚本。
2. 平台/租户管理后台（Vue3 + Element Plus）。
3. PC 品牌官网（双语、SEO、hreflang、在线表单）。
4. uni-app 多端（小程序/H5/App 壳）与 Flutter 双端 App。
5. 微信公众号 11 子页、消息配置、SSO、对象存储、菜单可视化。
6. 开放 API（AppKey/Secret 签名、配额、错误码、沙箱说明）。
7. 架构/数据库/API/部署文档 + 各端用户手册。
8. Nginx、systemd、备份/恢复、压测脚本。

## 外部依赖说明

真实微信登录/支付、模板消息、短信通道、SSO 身份源、云存储（S3/OSS/COS/MinIO）、App 商店上架、APNs/厂商推送需要运营方提供资质、密钥、模板 ID、合法域名与开发者账号。代码中已预留配置入口与降级/Mock 逻辑，凭证齐备后按文档替换即可。

## License

内部交付项目，版权归项目委托方所有。
