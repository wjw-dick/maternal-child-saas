# 数据库设计 | 母婴亲子 数字化多端平台

> 版本：v1.0　数据库：MySQL 8.0（本地 H2 MySQL 模式）　字符集：utf8mb4　对应任务：T02 / T03 / T21 / T25

## 1. 总体策略

| 项目 | 说明 |
| --- | --- |
| 隔离模型 | 共享库 + `tenant_id` 行级隔离 |
| 主键 | `BIGINT`，MyBatis-Plus `assign_id` 雪花 ID |
| 公共字段 | `id`、`tenant_id`、`created_at`、`updated_at`、`deleted` |
| 逻辑删除 | `deleted`：0 正常，1 删除；MyBatis-Plus 自动拼接 |
| 租户插件 | 自动为业务表追加 `tenant_id = ?` |
| 白名单 | `platform_*`、`sys_dict_*` 等平台/全局表不参与租户过滤 |
| 索引规范 | 业务表建立 `(tenant_id, ...)` 复合索引，查询条件优先走租户前缀 |
| 时间 | `DATETIME`，应用时区 `Asia/Shanghai` |
| 金额 | `DECIMAL(12,2)`，禁止浮点 |
| 敏感字段 | 手机号/证件号 AES 加密列 + 脱敏列 + 哈希列（用于去重/检索） |

## 2. 表清单（77 张）

### 2.1 平台运营域（8 张）

| 表 | 用途 | 关键字段 |
| --- | --- | --- |
| `platform_plan` | 套餐 | `code`、`price`、`billing_cycle`、五类配额、`trial_days` |
| `platform_tenant` | 租户 | `tenant_code`、`name`、`plan_id`、`status`、`trial_end_at`、`expire_at`、五类配额 |
| `platform_tenant_quota_usage` | 配额使用 | `tenant_id`、`used_seats`、`used_mp`、`used_storage_mb`、`used_sms`、`used_api`、`stat_date` |
| `platform_bill` | 账单 | `tenant_id`、`bill_no`、`period`、`amount`、`status`、`paid_at` |
| `platform_audit` | 平台审计 | `tenant_id`、`operator`、`module`、`action`、`result`、`ip` |
| `platform_tenant_admin` | 租户管理员映射 | `tenant_id`、`user_id`、`role` |
| `platform_notice` | 平台公告 | `title`、`content`、`scope`、`status`、`published_at` |
| `platform_notice_read` | 公告已读 | `notice_id`、`tenant_id`、`user_id`、`read_at` |

> 平台表不参与租户插件过滤；平台端只展示汇总与状态，不直接查询租户订单/会员明细。

### 2.2 系统与权限域（18 张）

| 表 | 用途 |
| --- | --- |
| `sys_user` | 用户（平台/租户/会员/员工关联），`user_type` 区分端 |
| `sys_role` | 角色（平台角色 + 租户 14 类角色） |
| `sys_menu` | 后台菜单/按钮权限树 |
| `sys_role_menu` | 角色-菜单授权 |
| `sys_audit_log` | 租户审计日志（含 `tenant_id`） |
| `sys_config` | 租户参数（站点/ICP/客服/SEO/支付/多语言开关） |
| `sys_language` | 语言启停 |
| `sys_i18n` | 翻译词条（`lang_code` + `i18n_key`） |
| `sys_menu_config` | 菜单可视化整树 JSON（按端） |
| `sys_storage_config` | 对象存储驱动与凭证 |
| `sys_message_channel` | 4 类消息通道启停与配置 |
| `sys_sms_channel` | 6 类短信渠道配置（互斥） |
| `sys_message_template` | 消息模板、触发节点、字段映射 |
| `sys_sso_config` | SSO 协议、根地址、映射、密钥 |
| `sys_attachment` | 附件元数据（路径、大小、类型、业务） |
| `sys_dict_type` | 全局/租户字典类型 |
| `sys_dict_item` | 字典项 |
| `open_app` | 开放平台应用（AppKey/Secret/范围/配额） |

### 2.3 会员与家庭域（3 张）

| 表 | 用途 | 关键约束 |
| --- | --- | --- |
| `biz_member` | 家庭会员 | `member_no`、`phone`（脱敏）、`phone_enc`、`phone_hash`、`id_card_enc`、`unionid`、`balance`、`points` |
| `biz_baby` | 宝宝档案 | `member_id`、生日、过敏、健康备注 |
| `biz_baby_growth` | 成长记录 | `baby_id`、身高、体重、记录日期、图片 |

去重规则：`phone_hash` 与 `unionid` 在同租户内唯一；UnionID 用于小程序/公众号/H5/App 多端互通。

### 2.4 门店与服务域（9 张）

`biz_store`、`biz_service`、`biz_staff`、`biz_staff_license`、`biz_schedule`、`biz_product`、`biz_sku`、`biz_stock_record`、`biz_fulfillment`。

- 门店：营业时间、服务范围、配送范围、经纬度。
- 服务：价格、时长、适用年龄、封面、详情。
- 员工：角色类型（护理师/育儿师/康复师等）、等级、证照。
- 排班：日期 + 时段 + 容量 + 已约数。
- 商品：SPU + SKU + 规格 JSON + 安全库存。
- 库存流水：`IN/OUT`、前后数量、业务单号。

### 2.5 交易与履约域（10 张）

`biz_order`、`biz_order_item`、`biz_payment`、`biz_refund`、`biz_fulfillment`、`biz_appointment`、`biz_dispatch`、`biz_checkin`、`biz_service_record`、`biz_attachment`。

订单状态：`PENDING → PAID → COMPLETED / REFUNDED / CANCELLED`；支付状态独立维护。预约状态：`PENDING → CONFIRMED → COMPLETED / CANCELLED`。

### 2.6 资产与营销域（8 张）

`biz_wallet`、`biz_wallet_flow`、`biz_points_rule`、`biz_points_flow`、`biz_coupon`、`biz_member_coupon`、`biz_activity`、`biz_activity_signup`。

- 储值：余额 + 流水（充值/消费/退款），所有变动必须写流水。
- 积分：规则 + 流水，订单支付按实付金额累加。
- 卡券：券模板 + 会员领取记录；过期由租户定时任务处理。
- 活动：报名记录、签到。

### 2.7 供应链与渠道域（9 张）

`biz_supplier`、`biz_supply_order`、`biz_supply_item`、`biz_supplier_statement`、`biz_distributor`、`biz_distributor_report`、`biz_commission_flow`、`biz_distributor_settlement`、`biz_commission_flow`。

- 供应商：供货单状态（待接/配送中/已送达）、签收、对账单。
- 渠道商：报单、审核、佣金明细、额度、结算。
- 所有金额字段 `DECIMAL(12,2)`，状态流转留痕。

### 2.8 内容与官网域（4 张）

`cms_article`（双语标题/正文/SEO）、`cms_banner`（按端）、`cms_case`（双语案例）、`cms_form_submission`（咨询/合作表单）。

### 2.9 微信生态域（15 张）

`wx_account`、`wx_mini_config`、`wx_follower`、`wx_tag`、`wx_message`、`wx_template`、`wx_menu_publish`、`wx_autoreply`、`wx_material`、`wx_draft`、`wx_publish_record`。

- 账号：AppID/Secret、服务器 URL、Token、EncodingAESKey、授权域名、二维码、类型、认证状态、API 配额。
- 粉丝：openid、unionid、标签、关注时间。
- 消息：类型、方向、errCode、原文。
- 模板：复用统一模板表，不产生第二套模板表。
- 菜单发布：草稿 → 校验（一级 ≤3、二级 ≤5）→ 发布记录，失败保留旧菜单。
- 素材/草稿/发表记录：三类素材、视频 thumb、多图文草稿、发表失败原因。

## 3. 关键字段与约束

### 3.1 租户字段

```sql
tenant_id BIGINT NOT NULL COMMENT '租户ID，平台表为0',
KEY idx_xxx_tenant (tenant_id, status, created_at)
```

### 3.2 敏感字段

```sql
phone       VARCHAR(32)  COMMENT '脱敏手机号，如 138****8000',
phone_enc   VARCHAR(255) COMMENT 'AES 加密手机号',
phone_hash  CHAR(64)     COMMENT 'SHA-256，用于去重/精确查询',
id_card     VARCHAR(32)  COMMENT '脱敏证件号',
id_card_enc VARCHAR(255) COMMENT 'AES 加密证件号'
```

- 列表与详情默认返回脱敏值；有权限场景才解密。
- 哈希仅用于等值查询，不可逆。
- 修改 `APP_CRYPTO_KEY` 会导致历史密文不可解密，变更前必须评估并迁移。

### 3.3 软删除与唯一性

MySQL 中逻辑删除与唯一索引同时使用时，建议唯一索引包含 `deleted` 或使用可空删除时间列。当前脚本对关键业务编号采用应用层校验 + 复合索引，避免删除后无法复用编号。

## 4. 索引设计

完整索引脚本见 `backend/src/main/resources/db/mysql-index.sql`。原则：

1. 所有业务表必须有 `(tenant_id, ...)` 前缀索引。
2. 列表页高频条件：`(tenant_id, status, created_at)`。
3. 订单/预约：`(tenant_id, order_no)`、`(tenant_id, appointment_no)`、`(tenant_id, member_id, created_at)`。
4. 会员：`(tenant_id, phone_hash)`、`(tenant_id, unionid)`。
5. 微信：`(tenant_id, openid)`、`(tenant_id, account_id, msg_type)`。
6. 审计：`(tenant_id, created_at)`、`(tenant_id, module, action)`。

## 5. 初始化与迁移

```bash
# 新库
mysql -uroot -p -e "CREATE DATABASE maternal_child DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p maternal_child < backend/src/main/resources/db/schema.sql
mysql -uroot -p maternal_child < backend/src/main/resources/db/data.sql
mysql -uroot -p maternal_child < backend/src/main/resources/db/mysql-index.sql
```

- `schema.sql`：77 张表结构，H2/MySQL 兼容写法。
- `data.sql`：演示租户 `demo`、平台/租户角色、菜单、参数、商品/服务/资讯等。
- `mysql-index.sql`：生产补充索引（H2 本地可不执行）。
- 增量变更：在 `db/migration/` 按 `V日期_说明.sql` 追加，禁止修改已上线脚本；禁止 `DROP/TRUNCATE`。

## 6. 备份与恢复

- 每日 `mysqldump --single-transaction --routines --triggers`，gzip 压缩，保留 7 份。
- 备份文件命名 `maternal_child_YYYYMMDD_HHMMSS.sql.gz`。
- 恢复前先做当前库快照；恢复脚本需二次确认，禁止直接覆盖生产库。
- 详见 `deploy/backup.sh`、`deploy/restore.sh`、`docs/deploy-baota.md`。

## 7. 数据边界与导出

- 租户后台导出仅限当前 `tenant_id`，由租户插件保证。
- 平台端导出仅平台汇总数据；如需下钻租户明细，必须只读并记录平台审计。
- 开放 API 仅返回应用所属租户数据，配额超限返回 `40205`。
- 微信回调只落库不执行业务的停用租户场景，避免停用后仍产生业务副作用。
