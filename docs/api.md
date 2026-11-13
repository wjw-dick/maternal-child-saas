# API 接口文档 | 母婴亲子 数字化多端平台

> 版本：v1.0　Base URL（生产）：`https://api.quantaida.cloud`　本地：`http://127.0.0.1:8080`　对应任务：T74 / T75 / T76 / T82

## 1. 通用约定

### 1.1 请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 登录接口外必填 | `Bearer <JWT>` |
| `X-Tenant-Code` | 公开/租户接口可选 | 租户编码，如 `demo`；也可通过子域名解析 |
| `Content-Type` | POST/PUT | `application/json` |
| `X-App-Key` / `X-Timestamp` / `X-Nonce` / `X-Sign` | 开放 API 必填 | 见 §9 |

### 1.2 统一响应

```json
{ "code": 0, "msg": "ok", "data": {} }
```

- `code=0` 成功；非 0 为业务错误。
- 分页 `data` 结构：`{ "records": [], "total": 0, "size": 10, "current": 1, "pages": 0 }`。
- 分页参数：`page`（默认 1）、`size`（默认 10）、`keyword`、以及实体字段过滤参数。

### 1.3 鉴权与端隔离

| 端 | 登录接口 | JWT `userType` | 路由前缀 |
| --- | --- | --- | --- |
| 平台 | `POST /api/platform/auth/login` | `PLATFORM` | `/api/platform/*` |
| 租户后台 | `POST /api/tenant/auth/login` | `TENANT` | `/api/tenant/*` |
| 小程序/H5/App 会员 | `POST /api/mini/auth/login` 等 | `MEMBER` | `/api/mini/*` |
| 一线/员工/供应商/渠道商 | 统一登录接口 + 角色 | `TENANT` | `/api/staff/*` 等 |
| 开放 API | AppKey/Secret 签名 | — | `/api/open/*` |

租户停用返回 `403`；账号类型不匹配返回 `403`；跨租户资源返回 `403` 并写审计。

## 2. 认证接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/platform/auth/login` | 平台登录 `{username,password}` |
| POST | `/api/tenant/auth/login` | 租户登录 `{username,password,tenantCode}` |
| GET | `/api/auth/me` | 当前用户 |
| POST | `/api/auth/change-password` | 修改密码 `{oldPassword,newPassword}` |
| POST | `/api/auth/logout` | 退出登录 |
| POST | `/api/auth/profile` | 更新个人资料 |

示例：

```bash
curl -X POST https://api.quantaida.cloud/api/tenant/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","tenantCode":"demo"}'
```

## 3. 平台运营端 `/api/platform`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/dashboard` | 平台看板：租户总数/活跃/到期预警/收入 |
| GET | `/tenants` | 租户列表（分页/关键词/状态） |
| GET | `/tenants/{id}` | 租户详情 |
| POST | `/tenants` | 开通租户（基本信息 + 套餐 + 配额 + 管理员） |
| PUT | `/tenants/{id}` | 编辑租户 |
| POST | `/tenants/{id}/status` | 停用/恢复 |
| POST | `/tenants/{id}/renew` | 续期 |
| DELETE | `/tenants/{id}` | 删除（二次确认 + 归档） |
| GET | `/tenants/plans` | 可选套餐 |
| GET | `/tenants/{id}/audits` | 租户审计 |
| GET | `/tenants/stats` | 租户统计 |
| GET/POST/PUT/DELETE | `/plans` | 套餐 CRUD |
| GET | `/bills` | 账单列表 |
| POST | `/bills/generate` | 生成账单 |
| POST | `/bills/{id}/pay` | 标记已付 |
| GET | `/bills/summary` | 计费汇总 |
| GET/POST/PUT/DELETE | `/notices` | 公告 CRUD；`POST /{id}/publish` 发布 |
| GET | `/audit` | 平台审计（可按租户筛选） |

平台端只返回汇总与租户状态，不提供租户订单/会员明细查询入口。

## 4. 租户后台 `/api/tenant`

### 4.1 通用 CRUD

所有资源遵循同一套接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/tenant/{resource}` | 分页列表，支持 `page/size/keyword` + 字段过滤 |
| GET | `/api/tenant/{resource}/all` | 最多 500 条下拉数据 |
| GET | `/api/tenant/{resource}/{id}` | 详情 |
| POST | `/api/tenant/{resource}` | 新增 |
| PUT | `/api/tenant/{resource}/{id}` | 更新 |
| DELETE | `/api/tenant/{resource}/{id}` | 逻辑删除 |

`resource` 列表（与后端控制器路径一致）：

`members`、`baby`、`stores`（`store`）、`service`、`staff`、`staff-licenses`、`schedule`、`product`、`sku`、`stock-records`、`orders`、`appointments`、`wallets`、`coupon`、`activities`（`activity`）、`activity-signups`、`supplier`、`supply-orders`、`supply-items`、`supplier-statements`、`distributor`、`distributor-reports`、`commission-flows`、`distributor-settlements`、`article`、`banner`、`cases`、`form-submissions`、`attachments`、`audit-logs`、`config`、`menus`、`menu-config`、`roles`、`users`、`languages`、`i18n`、`open-apps`。

> 权限前缀与资源对应；跨租户 ID 访问返回 `403` 并记录 `cross-tenant-access` 审计。

### 4.2 业务动作

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/orders/create` | 下单 `{memberId,storeId,items:[{itemType,itemId,qty}],idempotencyKey}` |
| POST | `/orders/{id}/pay` | 支付（Mock/微信待接入） |
| POST | `/orders/{id}/refund` | 退款 |
| POST | `/orders/{id}/cancel` | 取消 |
| GET | `/orders/{id}/items` | 订单明细 |
| POST | `/appointments/create` | 预约（到店/上门） |
| POST | `/appointments/{id}/dispatch` | 派单 |
| POST | `/appointments/verify` | 核销码核销 |
| POST | `/appointments/{id}/reschedule` | 改约 |
| POST | `/appointments/{id}/cancel` | 取消 |
| GET | `/wallets/{memberId}` | 储值账户 |
| GET | `/wallets/{memberId}/flows` | 储值流水 |
| POST | `/wallets/recharge` | 充值 |
| POST | `/wallets/consume` | 消费 |
| GET | `/dashboard` | 租户看板 |
| GET | `/report` | 报表（会员/预约/订单/营收/储值/分佣） |
| GET | `/notices` | 平台公告 |
| POST | `/notices/{id}/read` | 标记已读 |

### 4.3 配置类

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET/PUT | `/config` | 系统参数（站点/ICP/客服/SEO/支付/多语言） |
| GET | `/storage-config` | 存储配置 |
| GET | `/storage-config/drivers` | 5 种驱动 |
| POST | `/storage-config/test` | 连接测试 |
| PUT | `/storage-config` | 保存（切换二次确认） |
| GET | `/message-config` | 消息通道 |
| PUT | `/message-config/channel` | 4 通道启停（互斥） |
| PUT | `/message-config/sms` | 6 短信渠道（互斥） |
| POST | `/message-config/test` | 测试发送 |
| GET/POST/PUT/DELETE | `/message-templates` | 模板 CRUD |
| GET | `/sso-config` | SSO 配置 |
| GET | `/sso-config/protocols` | CAS/OAuth2/OIDC/Sa-Token |
| POST | `/sso-config` | 保存 |
| POST | `/sso-config/authorize-url` | 拼接授权地址 |
| GET/PUT/DELETE | `/menu-config` | 菜单可视化整树 |
| GET/POST/PUT/DELETE | `/roles` | RBAC 角色 |
| GET/POST | `/open-apps` | 开放应用注册 |

## 5. 公开接口 `/api/public`（免登录）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/home?terminal=PC` | 官网首页聚合（Banner/服务/商品/门店/资讯/案例） |
| GET | `/services`、`/services/{id}` | 服务列表/详情 |
| GET | `/products`、`/products/{id}` | 商品列表/详情（含 SKU） |
| GET | `/stores` | 门店 |
| GET | `/articles`、`/articles/{id}` | 资讯 |
| GET | `/cases` | 客户案例 |
| GET | `/activities` | 亲子活动 |
| GET | `/config` | 前台参数 |
| GET | `/i18n/{lang}` | 翻译词条 `zh` / `en` |
| GET | `/menus/{terminal}` | 菜单可视化结果（PC/MINI/H5/APP） |
| POST | `/forms` | 咨询/合作表单 `{formType,name,phone,content,source}` |

## 6. 小程序 / H5 / App `/api/mini` 与 `/api/auth`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/mini/auth/login` | 微信 code 登录（Mock 可传 unionid） |
| GET | `/api/mini/home` | 首页 |
| GET | `/api/mini/services`、`/products`、`/stores`、`/activities` | 展示 |
| POST | `/api/mini/appointments` | 预约 |
| GET | `/api/mini/appointments/mine` | 我的预约 |
| POST | `/api/mini/orders` | 下单 |
| GET | `/api/mini/orders/mine` | 我的订单 |
| POST | `/api/mini/orders/{id}/pay` | 支付 |
| GET | `/api/mini/member/center` | 会员中心 |
| GET/POST | `/api/mini/babies` | 宝宝档案 |
| GET/POST | `/api/mini/babies/{id}/growth` | 成长记录 |
| POST | `/api/mini/activities/{id}/signup` | 活动报名 |
| GET | `/api/mini/messages` | 消息中心 |
| PUT | `/api/mini/profile` | 个人资料 |

## 7. 一线 / 员工 / 供应商 / 渠道商

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/staff/workbench` | 一线工作台 |
| POST | `/api/staff/appointments/{id}/accept` | 接单 |
| POST | `/api/staff/checkin` | GPS 签到 |
| POST | `/api/staff/records` | 护理记录 |
| GET | `/api/staff/records/{appointmentId}` | 记录详情 |
| POST | `/api/staff/appointments/{id}/complete` | 完成确认 |
| GET | `/api/staff/income` | 收入 |
| GET | `/api/staff/schedules` | 排班 |
| GET | `/api/staff/services` | 可服务项目 |
| GET | `/api/supplier/supply-orders` | 供货单 |
| GET | `/api/supplier/supply-orders/{id}/items` | 供货明细 |
| POST | `/api/supplier/supply-orders/{id}/sign` | 签收 |
| GET | `/api/supplier/statements` | 对账单 |
| GET | `/api/supplier/profile` | 供应商资料 |
| POST/GET | `/api/distributor/reports` | 报单提交/列表 |
| GET | `/api/distributor/commissions` | 佣金明细 |
| GET | `/api/distributor/credit` | 额度 |
| GET | `/api/distributor/subordinates` | 下级 |
| GET | `/api/distributor/profile` | 渠道商资料 |

## 8. 微信公众号 `/api/wx`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET/POST/DELETE | `/api/tenant/wx/accounts` | 公众号账号 |
| POST | `/api/tenant/wx/accounts/{id}/reset-quota` | 清空 API 配额（二次确认） |
| GET/POST | `/api/tenant/wx/mini-config` | 小程序凭证 |
| GET | `/api/tenant/wx/stats` | 数据统计 |
| GET/POST/DELETE | `/api/tenant/wx/tags` | 标签 |
| POST | `/api/tenant/wx/tags/sync` | 同步标签 |
| GET | `/api/tenant/wx/followers` | 粉丝 |
| POST | `/api/tenant/wx/followers/{id}/tags` | 写回标签 |
| GET | `/api/tenant/wx/messages` | 消息流水 |
| GET | `/api/tenant/wx/templates` | 模板消息（统一模板表） |
| POST | `/api/tenant/wx/templates/sync` | 同步模板 |
| GET/POST/DELETE | `/api/tenant/wx/menu` | 菜单草稿/发布/清空 |
| GET/POST/DELETE | `/api/tenant/wx/autoreplies` | 自动回复 |
| GET/POST/DELETE | `/api/tenant/wx/materials` | 素材 |
| GET/POST | `/api/tenant/wx/drafts` | 图文草稿 |
| POST | `/api/tenant/wx/drafts/{id}/publish` | 发表 |
| GET | `/api/tenant/wx/publish-records` | 发表记录 |
| POST | `/api/wx/callback` | 微信服务器回调（验签/落库） |

## 9. 开放 API `/api/open`

### 9.1 签名规则

```
sign = SHA256(appKey + timestamp + nonce + appSecret)
```

请求头：

```
X-App-Key: ak_xxxxxxxx
X-Timestamp: 1763000000000
X-Nonce: 8f3c2a...
X-Sign: 64位小写十六进制
```

- `timestamp` 为毫秒，服务端允许 ±5 分钟。
- `nonce` 建议 UUID，生产需配合 Redis 做 5 分钟防重放。
- 签名失败返回 `401`；API 配额超限返回 `40205`。

### 9.2 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/open/products` | 商品列表 |
| GET | `/api/open/orders/{orderNo}` | 订单详情 |
| GET | `/api/open/members/{phone}` | 会员查询（手机号哈希匹配） |
| GET | `/api/open/error-codes` | 错误码（免签名） |

### 9.3 示例（Node.js）

```js
import crypto from 'node:crypto'

const appKey = 'ak_xxx'
const appSecret = 'your-secret'
const timestamp = Date.now().toString()
const nonce = crypto.randomUUID().replaceAll('-', '')
const sign = crypto.createHash('sha256').update(appKey + timestamp + nonce + appSecret).digest('hex')

const res = await fetch('https://api.quantaida.cloud/api/open/products', {
  headers: { 'X-App-Key': appKey, 'X-Timestamp': timestamp, 'X-Nonce': nonce, 'X-Sign': sign }
})
console.log(await res.json())
```

### 9.4 沙箱

- 本地启动 `h2` profile 即为沙箱，`data.sql` 提供演示应用与租户。
- 沙箱环境不产生真实微信/支付/短信外部调用。
- 生产前请使用独立 AppKey，并开启 `REDIS_ENABLED=true` 做 nonce 防重放。

## 10. 错误码

| code | 含义 |
| --- | --- |
| `0` | 成功 |
| `400` | 参数错误 |
| `401` | 未登录/签名失败/账号密码错误 |
| `403` | 越权/端隔离/租户停用/账号类型不匹配 |
| `40301` | 租户已停用或到期 |
| `404` | 资源不存在 |
| `429` | 限流或重复提交 |
| `40201` | 席位超限 |
| `40202` | 公众号数量超限 |
| `40203` | 存储超限 |
| `40204` | 短信超限 |
| `40205` | API 配额超限 |
| `50010` | 存储驱动未实现/云端驱动未接入 |
| `500` | 服务端异常 |

## 11. 限流与防重

| 场景 | 策略 |
| --- | --- |
| 登录 | 同账号/IP 频率限制 |
| 下单 | `idempotencyKey` + Redis SETNX（10 分钟） |
| 预约 | `idempotencyKey` + 时段容量校验 |
| 支付 | 订单状态机 + 重复支付幂等 |
| 报单 | 渠道商 + 业务键防重 |
| 开放 API | 租户/应用维度配额 + 时间窗限流 |

Redis 未启用时自动降级单机限流，多实例生产必须启用 Redis。

## 12. 员工端接口（/api/employee，TENANT 员工账号）

数据边界：门店范围取自登录账号 `storeId`，不接受前端传入；所有查询由租户插件隔离。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/employee/overview` | 今日预约/待核销/已完成/今日订单/低库存概览 |
| GET | `/api/employee/appointments?date=` | 门店预约列表（默认今天） |
| POST | `/api/employee/appointments/verify` | 核销，body `{code}`，状态 CONFIRMED/PENDING → COMPLETED |
| GET | `/api/employee/members?keyword=` | 会员查询（编号/姓名/手机号 hash） |
| POST | `/api/employee/members` | 开卡：`{name,phone,babyName?,babyBirthday?,remark?}`，自动建储值账户 |
| GET | `/api/employee/members/{id}/wallet` | 会员、储值账户与流水 |
| POST | `/api/employee/members/{id}/recharge` | 充值 `{amount,remark?}` |
| POST | `/api/employee/members/{id}/consume` | 储值消费 `{amount,remark?}` |
| GET | `/api/employee/skus?keyword=` | SKU 库存查询 |
| POST | `/api/employee/stock/adjust` | 库存操作 `{skuId,qty,recordType:IN/OUT/ADJUST,remark?}` |
| GET | `/api/employee/orders?date=` | 当日订单 |
| POST | `/api/employee/orders/{id}/complete` | 已支付订单完成（仅 PAID 可操作） |
| GET | `/api/employee/schedules` | 门店近期排班 |
| GET | `/api/employee/brief` | 简报：预约数/完成数/实收/新增会员 |

> 主键统一以字符串返回（雪花 ID 超出 JS 安全整数）。库存暂不支持跨门店调拨。
## 13. 一线端接口（/api/staff，护理师/育儿师账号）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/staff/workbench` | 今日工单、待处理、已完成、收入摘要 |
| POST | `/api/staff/appointments/{id}/accept` | 接单，`PENDING → CONFIRMED` |
| GET | `/api/staff/appointments/{id}` | 工单详情：预约、会员、宝宝、服务、签到、护理记录 |
| POST | `/api/staff/appointments/{id}/checkin` | GPS 签到，`{checkinType,lng,lat,address,photoUrl}`，置 `SERVING` |
| POST | `/api/staff/appointments/{id}/records` | 护理记录，`{babyTemp,babyWeight,content,images,signedUrl}` |
| POST | `/api/staff/appointments/{id}/complete` | 完成服务，`{signUrl}`，置 `COMPLETED` |
| GET | `/api/staff/income` | 累计服务金额与完成单数 |
| GET | `/api/staff/income/detail` | 已完成工单明细（服务名/日期/时段/金额/时长，最近 50 单） |
| GET | `/api/staff/schedules` | 我的排班 |
| GET | `/api/staff/notices` | 已发布平台公告（最近 20 条） |
| GET | `/api/staff/profile` | 护理师档案与账号 |
| POST | `/api/staff/files` | 现场照片/签名上传（multipart `file`，`bizType` 可选） |

> 预约详情/签到/记录/完成均校验预约归属，非本护理师返回 `403`。主键统一字符串返回。
## 14. 供应商端接口（/api/supplier）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/supplier/supply-orders` | 我的供货单 |
| GET | `/api/supplier/supply-orders/{id}/items` | 供货单明细 |
| POST | `/api/supplier/supply-orders/{id}/sign` | 送货签收，库存入库并写流水 |
| GET | `/api/supplier/statements` | 对账单（含结算状态） |
| GET | `/api/supplier/qualifications` | 资质证照列表 |
| POST | `/api/supplier/qualifications` | 新增资质，`{licenseType,licenseNo,fileUrl,expireDate}`，审核状态 PENDING |
| GET | `/api/supplier/notices` | 平台公告（ALL/SUPPLIER） |
| GET | `/api/supplier/profile` | 供应商档案 |
| POST | `/api/supplier/files` | 资质/证件上传 |

## 15. 渠道商端接口（/api/distributor）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/distributor/reports` | 报单，按佣金规则生成佣金流水 |
| GET | `/api/distributor/reports` | 报单记录与审核状态 |
| GET | `/api/distributor/commissions` | 佣金流水 |
| GET | `/api/distributor/credit` | 授信额度/已用/可用 |
| GET | `/api/distributor/orders` | 我的订单（报单关联订单） |
| GET | `/api/distributor/subordinates` | 下级渠道商 |
| GET | `/api/distributor/settlements` | 结算记录 |
| GET | `/api/distributor/notices` | 平台公告（ALL/DISTRIBUTOR） |
| GET | `/api/distributor/profile` | 渠道商档案 |

## 16. 积分与售后接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET/POST/PUT/DELETE | `/api/tenant/points-rules` | 积分规则 CRUD |
| GET | `/api/tenant/points-flows` | 积分流水 |
| POST | `/api/tenant/points/adjust` | 调整积分 `{memberId,points,remark,bizNo}`，同步会员余额并落流水 |
| GET | `/api/tenant/points/members/{memberId}/flows` | 会员积分流水 |
| POST | `/api/mini/orders/{id}/aftersale` | 会员申请售后 `{reason}`，生成 PENDING 退款单 |
| GET | `/api/mini/orders/{id}/aftersale` | 售后申请记录 |