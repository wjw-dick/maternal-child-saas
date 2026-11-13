# 验收执行报告（2026-11-13）

> 本报告记录母婴亲子多租户 SaaS 平台本轮实际执行的验收动作与结果，作为 checklist.md 勾选依据。未执行或未通过项在第九章列明，不默认通过。

## 一、结论摘要

| 维度 | 结果 | 证据 |
| --- | --- | --- |
| 后端自动化测试 | 通过 9/9 | `InfrastructureTest` 4/4、`TenantIsolationTest` 5/5 |
| 后端接口冒烟 | 通过 | 公开 9 项、平台 6 项、租户 50 项列表接口均 `code=0` |
| 多租户隔离 | 通过 | 开通 0.22s、跨租户 403+审计、租户 B 列表不串数据、平台审计可按租户筛选 |
| SSO | 通过（沙箱） | 启用→授权链接→模拟登录签发 token；停用后登录 403 |
| 管理后台构建 | 通过 | `vite build` 成功，产物 `admin/dist` |
| PC 官网构建 | 通过 | `vite build` 成功，产物 `site/dist` |
| uni-app H5 构建 | 通过 | `npm run build:h5`，Build complete |
| Flutter | 通过 | `flutter analyze` 0 issue；`flutter test` 全部通过 |
| 管理后台 UI | 通过（核心流程） | 租户/平台双入口、工作台、会员、公众号 11 子页、对象存储、消息配置、修改密码 |
| 员工端（T59–T61） | 通过（除调拨） | 核销、开卡储值、库存入库/出库/盘点、订单完成、排班、简报、改密 |
| 一线端（T56–T58） | 通过（T55 排班页除外） | GPS 签到、拍照、护理记录、电子签名、完成服务、收入工时、协议/改密 |

结论：**核心链路与多租户隔离已可演示验收**；云存储 SDK、真实微信/支付/短信凭证、MySQL/Redis/RabbitMQ 生产部署与压测仍属未完成或外部依赖项，详见第九章。

## 二、验收环境

| 项 | 值 |
| --- | --- |
| 执行日期 | 2026-11-13 |
| 操作系统 | Windows（本地环境 `client:582478:2420487470220569`） |
| JDK | Eclipse Adoptium 17.0.20.1 |
| Spring Boot | 3.2.5 |
| 数据库 | H2（MySQL 兼容模式，内存库，启动自动执行 `schema.sql` / `data.sql`） |
| 后端端口 | 18080 |
| 管理后台 | Vite 5.4.21，开发端口 5174（5173 被其他项目占用） |
| 前端 API 代理 | `VITE_API_TARGET=http://127.0.0.1:18080` |

## 三、自动化测试

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- InfrastructureTest
  - idempotentRejectsDuplicateSubmit        防重提交
  - rateLimitBlocksOverLimit                限流阈值
  - numberGeneratorProducesUniqueTenantNumbers  编号唯一
  - sandboxSsoLoginIssuesToken              SSO 沙箱登录签发 token

Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- TenantIsolationTest
  - listOnlyContainsOwnTenantData           列表不串租户
  - crossTenantResourceIdReturns403AndAudits 跨租户资源 403+审计
  - platformAccountCannotAccessTenantBusinessApi
  - tenantAccountCannotAccessPlatformApi
  - demoAdminCanReadTenantDashboard
```

执行命令：`cd backend && mvn test`，退出码 0。

> 本轮修复前 `InfrastructureTest.sandboxSsoLoginIssuesToken` 失败（`$.code` 实际 401），原因是 `/api/sso/login` 未加入免登录白名单；已修复并复测通过。

## 四、接口冒烟

统一租户 `demo` / 平台超管 `admin`，密码 `admin123`。

- 公开接口（免登录）：`/api/public/home`、`services`、`products`、`stores`、`articles`、`cases`、`activities`、`config`、`i18n/zh-CN`、`menus/PC` 全部 `code=0`。
- 平台端：`dashboard`、`tenants`、`plans`、`bills`、`audit`、`notices` 全部 `code=0`。
- 租户端列表：members、baby、service、product、sku、store、schedule、orders、appointments、coupon、member-coupons、activity、activity-signups、staff、staff-licenses、supplier、supply-orders、supply-items、supplier-statements、distributor、distributor-reports、distributor-settlements、commission-flows、article、banner、cases、form-submissions、attachments、audit-logs、roles、users、menus、config、i18n、languages、message-templates、open-apps、stock-records、menu-config、message-config、storage-config、sso-config、wx/accounts、wx/followers、wx/tags、wx/messages、wx/templates、wx/stats 全部 `code=0`。
- 钱包按会员查询：`/api/tenant/wallets/1`、`/api/tenant/wallets/1/flows` 均 `code=0`（该控制器按设计无“全部钱包”列表接口）。

## 五、多租户隔离实测

| 场景 | 操作 | 结果 |
| --- | --- | --- |
| 开通租户 | `POST /api/platform/tenants`（iso-b3，基础版） | `code=0`，耗时 **0.2176s**，状态 ACTIVE，管理员账号自动创建 |
| 租户可用 | `POST /api/tenant/auth/login`（iso-b3 / admin） | `code=0`，签发 token，可正常建档 |
| 跨租户直连 | 租户 A 访问租户 B 会员 ID | `code=403`，提示“无权访问其他租户数据”，审计新增 `cross-tenant-access` |
| 列表隔离 | 租户 B 查会员列表 | 仅返回本租户 1 条，无租户 A 数据 |
| 平台审计 | `GET /api/platform/audit?tenantId={B}` | 命中 `tenant:create` 1 条，tenantId 正确 |
| 平台/租户互访 | 租户 token 调 `/api/platform/*`；平台 token 调 `/api/tenant/*` | 均 `code=403`；前端进入 `/platform/*` 被重定向登录页 |

> 补充修复：平台审计原未写入 `tenant_id`，导致“按租户筛选”为空；已为租户开通/编辑/状态/续期/删除、账单支付写入租户维度。

## 六、SSO 实测（沙箱模式）

1. `POST /api/tenant/sso-config` 启用 OIDC，保存成功，`enabled=1`。
2. `GET /api/tenant/sso-config` 回显 `clientSecret=******`（掩码）。
3. `POST /api/tenant/sso-config/authorize-url` 仅填根地址，正确拼出：
   `https://idp.example.com/oidc/authorize?client_id=mc-client&response_type=code&scope=openid&redirect_uri=https://demo.quantaida.cloud/login`
4. `POST /api/sso/login`（`SSO_MOCK=true`）返回 `code=0`、token、`mapping=username`。
5. 停用后立即再登录，返回 `code=403`“SSO 未启用，请使用账号密码登录”，降级账号密码可用。

## 七、前端构建

| 端 | 命令 | 结果 |
| --- | --- | --- |
| 管理后台 | `cd admin && npm run build` | 成功，产物 `admin/dist` |
| PC 官网 | `cd site && npm run build` | 成功，产物 `site/dist` |
| uni-app H5 | `cd uniapp && npm run build:h5` | 成功，产物 `uniapp/dist/build/h5` |
| Flutter | `flutter analyze` / `flutter test` | 0 issue / All tests passed |

## 八、管理后台 UI 实测

- 登录页：租户后台 / 平台运营双入口，租户编码 + 账号密码；平台入口无租户编码字段。
- 租户端：经营工作台（会员 2、今日预约、今日订单、累计实收 127.00、经营提醒、快捷入口、平台公告）；侧边栏分组为概览/会员/预约/交易/门店服务/营销/供应链/渠道/员工/公众号/官网/系统/报表。
- 家庭会员：列表 2 条，手机号显示 `138****0001`、`139****0002`（脱敏生效），支持关键词查询、新增、编辑、删除、分页。
- 公众号运营：**11 个子页**（账号管理、数据统计、标签管理、粉丝管理、消息记录、模板消息、菜单发布、自动回复、素材管理、图文草稿、发表记录）；账号台账与小程序配置分区；数据统计含 **4 张图**（粉丝增减、累计用户、消息概况、接口分析），支持近 7/15/30 天切换与空态。
- 对象存储：5 张驱动卡片单选（LOCAL/S3/OSS/COS/MINIO），选中后展示配置表单、连接测试、保存。
- 消息配置：4 条通道（APP_PUSH/INNER/MINI_SUBSCRIBE/MP_TEMPLATE）+ 短信渠道全局互斥单选。
- 个人中心：用户菜单含“修改密码”弹窗（原密码/新密码/保存）。
- 平台端：平台看板（租户总数 2、正常运营 2、到期预警、累计回款、套餐分布、账单概览）；租户管理列表（编码/名称/行业/联系人/电话/套餐/五类配额/操作）；平台端不出现租户业务菜单。

## 八·补充、员工端补齐与验收（T59–T61，2026-11-13 下午）

此前员工端只有后台页面，uni-app 无入口。本次补齐为独立端：

- 后端新增 `EmployeeController`（`/api/employee/*`）：今日概览、预约列表、核销、会员查询/开卡、储值充值/消费/流水、SKU 查询、库存入库/出库/盘点、订单列表/完成、排班、简报。
- 前端新增 4 页：`pages/employee/index`（工作台+核销+订单+简报+排班）、`wallet`（开卡/储值）、`stock`（扫码/库存操作）、`profile`（改密/退出）。
- 角色识别：员工账号（前台/店长/财务/库管/人事）登录后进入“员工工作台”，与护理师/供应商/渠道商工作台分流。

实测记录（账号 `clerk/admin123`，门店 1001）：

| 场景 | 结果 |
| --- | --- |
| 登录 | `code=0`，`/api/auth/me` 返回 `roleCode=FRONT_DESK / roleName=前台` |
| 工作台 | 今日预约 1、待核销 1、今日订单 2、简报实收 195、排班 2 条 |
| 核销 | 错误码提示“核销码无效”；正确码 `836521` → 预约状态 `COMPLETED` |
| 开卡 | 新会员 `MB20261113132304000001`，自动建储值账户，可带宝宝档案 |
| 储值 | 充值 100 → 余额 100；消费 30 → 余额 70；流水 2 条；另充值 88 后余额 88 |
| 库存 | SKU 查询 3 条；入库 5 → 库存 120→125，写入库存流水 |
| 订单 | 创建并支付（MOCK）→ 员工端完成，状态 `PENDING→PAID→COMPLETED` |
| 改密 | 原密码错误返回 `400 原密码错误`；页面校验新密码≥6 位与两次一致 |

**同时修复**：雪花 ID 超出 JS 安全整数（2^53），前端请求详情时 ID 被四舍五入导致“会员不存在”。已在 `BaseEntity.id` 统一 `@JsonSerialize(using = ToStringSerializer.class)`，全部实体主键以字符串返回，复测通过，后端测试仍 9/9。

未完成：跨门店库存“调拨”未实现（当前 `biz_sku` 无门店维度，仅有总库存），checklist §九第 2 项因此保持未勾选。

## 八·补充 2、一线端补齐与验收（T56–T58，2026-11-13 下午）

此前一线端仅有工作台统计与接单/完成按钮，无现场作业能力。本次补齐：

- 后端（`RoleAppController`）新增：`GET /api/staff/appointments/{id}`（预约+会员+宝宝+服务+签到+记录）、`POST /{id}/checkin`（GPS 经纬度/地址/现场照片，签到后置 `SERVING`）、`POST /{id}/records`（体温/体重/护理内容/多图）、`GET /api/staff/income/detail`（已完成工单明细，最近 50 单）、`GET /api/staff/notices`（已发布平台公告）、`GET /api/staff/profile`（护理师档案）。`POST /{id}/complete` 接收客户签名 URL。
- 前端新增 3 页：`pages/staff/appointment`（工单详情、GPS 定位、现场拍照、护理记录、canvas 电子签名、完成服务、历史记录）、`staff/income`（收入/完成单/累计工时/明细）、`staff/profile`（档案、平台公告、服务协议、改密、退出）。
- 工作台今日工单可点击进入详情；新增“收入与工时 / 消息·改密”入口；页面 `onShow` 自动刷新，完成服务返回后统计即时更新。

实测记录（账号 `nurse/admin123`，护理师 1201）：

| 场景 | 结果 |
| --- | --- |
| 工单详情 | `AP20260001`，会员张晓雨、宝宝小葡萄、服务新生儿居家护理、¥399/120 分钟；客户手机号脱敏展示 |
| GPS 签到 | `code=0`，`checkinType=GPS`，经纬度 `113.321/23.135`，地址“广州市天河区珠江新城 8 号”，预约转 `SERVING` |
| 现场拍照 | `POST /api/staff/files` 上传成功（`/files/tenants/1/staff-service/202611/...png`），`GET` 返回 200；签到记录关联照片 |
| 护理记录 | 保存体温 36.6 ℃、体重 8.1 kg、护理内容，写入 `biz_service_record`，页面历史记录立即可见 |
| 电子签名 | canvas 手写 → `canvasToTempFilePath` 导出 → 上传成功（提示“签名已上传”），确认弹窗后完成服务 |
| 完成服务 | 预约转 `COMPLETED`，返回工作台后统计 `待处理 0 / 已完成 1 / 服务金额 ¥399` |
| 收入与工时 | ¥399、完成单 1、累计工时 2.0h；明细显示“新生儿居家护理 2026-11-13 09:00-12:00 · 120 分钟 ¥399” |
| 个人中心 | 显示“李护理 · 高级”，服务协议 4 条，公告区可加载（种子无已发布公告故为空），改密入口与原密码校验已联通 |
| 越权校验 | `requireOwnAppointment` 校验预约归属，非本护理师返回 `403 该预约不属于当前护理师` |

**同时修复**：

1. 工作台仅在 `onMounted` 加载，完成服务返回后数据不刷新；改为 `onShow` 加载，返回即刷新。
2. 收入明细原用 `queryForList` + 驼峰别名，H2 返回列名被转小写导致服务名/日期为空；改为 `RowMapper` 显式映射，前端正常显示。

**限制与未验证**：

- H5 浏览器定位需用户授权，自动化会话未授予地理权限，`获取定位`未取得坐标；GPS 签到链路已由接口实测覆盖，真机（小程序/App）使用 `uni.getLocation` 不受此限。
- 现场拍照的文件选择对话框在自动化会话中未实际点选；上传接口与文件访问已用真实 PNG 验证，签名图片亦通过同一上传通道落库。
- 种子的平台公告表无 `PUBLISHED` 记录，公告区显示空态；“消息”当前复用平台公告，尚无护理师个人站内信表。
- checklist §八第 1 项（T55 我的排班）未勾选：工作台已展示今日工单与接单，独立排班日历页尚未实现。

## 八·补充 3、供应商 / 渠道商 / 积分 / App 补齐（T63–T67、T70）

- 供应商端：新增资质表 `biz_supplier_qualification` 与 `GET/POST /api/supplier/qualifications`；个人中心页 `pages/supplier/profile`（资质上传/列表、平台公告、修改密码、退出）；工作台对账单显示结算状态并提供个人中心入口。
- 渠道商端：新增 `GET /api/distributor/settlements`、`GET /api/distributor/orders`、`GET /api/distributor/notices`；工作台展示结算记录、我的订单、我的下级；新增 `pages/distributor/profile`（下级、结算、公告、修改密码、退出）。
- 积分：新增 `PointsController`（调整积分并落流水）、`PointsRuleController`、`PointsFlowController`；后台新增“积分规则 / 积分流水”两个菜单。
- App（Flutter）：新增 `pages/order_page.dart`（订单列表、MOCK 支付、售后申请），个人中心接入“我的订单”、修改密码、服务协议；后端新增 `POST /api/mini/orders/{id}/aftersale` 与查询。
- H5：`App.vue` 捕获 `source/ref/utm_source` 并写入 `shareSource`，请求头带 `X-Source`；服务列表新增分享按钮（复制带来源标识的链接）；订单支付后弹窗确认并回到列表。

实测记录：

| 场景 | 结果 |
| --- | --- |
| 供应商登录 | `supplier01/admin123` 进入供应商工作台，显示供货单 `SO20260001`、对账单 2 条（SETTLED/UNSETTLED） |
| 供应商资质 | 上传接口新增资质成功（`auditStatus=PENDING`），列表返回 1 条；预置资质显示“食品经营许可证 · APPROVED” |
| 供应商消息 | 平台公告 2 条可见；个人中心协议/改密入口正常 |
| 渠道商登录 | `dist01/admin123`：授信 50000、已用 1200、可用 48800；报单、佣金、结算 2 条、下级 1 条 |
| 渠道商订单 | 报单关联订单后“我的订单”显示 `ORD20260001` 与报单号 |
| 渠道商个人中心 | 下级“番禺母婴渠道”、结算 2 条、公告 2 条、改密入口正常 |
| 积分 | 后台给会员 2001 调整 +10，余额 330，流水新增 1 条；积分规则 1 条 |
| App | `flutter analyze` 0 issue、`flutter test` 通过；订单页支持支付与售后申请 |

限制：供应商/渠道商“消息”复用平台公告，尚无各自站内信表；Flutter 厂商推送、多语言未接入（需 APNs/厂商通道与文案资源）；真实微信/支付宝支付仍为 MOCK。

补充实测（T55 / T62 / T65）：护理师工作台显示“我的排班 2026-11-13”；供应商供货单 `SO20260001` 由 `PENDING` 发货为 `SHIPPED`，再签收为 `SIGNED`；渠道商工作台提交报单金额 100，生成 `RP20261113140530000001` 并写入佣金流水。

## 九、未通过 / 未验证项（不得视为已完成）

1. **云存储驱动未实现**：`StorageService` 仅 LOCAL 驱动可写入；选择 S3/OSS/COS/MINIO 上传会返回 `50010 未装配 SDK`。需引入各厂商 SDK 与凭证后补齐。
2. **消息配置形态与闸门**：当前为表格行 + JSON 配置，不是 PRO 要求的 4 张通道卡 + 配置弹窗“测试通过才亮保存”；已具备测试按钮与渠道互斥，但闸门未强制。
3. **后台 UI 模板**：布局为自研 `Layout.vue`，未采用 vue-pure-admin / soybean-admin 模板，与 PRO 第八章“基于模板、非手写 layout”存在偏差。
4. **后台缺少独立入口的模块**：储值账户/流水、积分规则/流水、供应商对账、渠道结算、会员卡券、附件管理已有后端接口或数据表，但未配置独立菜单页（部分页面可由通用 CRUD 扩展）。
5. **生产中间件未验证**：MySQL 8、Redis、RabbitMQ、定时任务分片、消息队列消费仅代码与配置就绪，本轮使用 H2 + 单机降级，未做真机联调。
6. **部署与压测未执行**：宝塔 Nginx/通配符证书/仅 IPv4、systemd 守护、备份恢复演练、`deploy/bench.sh` 压测均未在本轮执行。
7. **外部资质依赖**：真实微信服务号/小程序 AppID、模板 ID、合法域名、微信支付/支付宝、短信通道、SSO IdP、APNs/厂商推送、App 商店账号均需运营方提供。
8. **未逐项实测**：租户停用后回调只落库、配额超限拦截、租户删除归档、多语言切换数据不串、资质到期预警等场景仍需专项用例。
9. **员工端库存调拨**：仅支持入库/出库/盘点，跨门店调拨需要 `biz_sku` 增加门店/仓库维度，当前未实现。
10. **站内信**：员工/供应商/渠道商“消息”复用 `platform_notice`，无个人站内信与已读状态。
11. **Flutter 推送与多语言**：订单/售后/改密/协议已补齐，APNs/Android 厂商推送、微信订阅兜底与多语言资源未接入。
12. **H5 支付回跳**：当前 MOCK 支付在页内弹窗确认，未接微信/支付宝真实收银台回跳与验签。

## 十、下一步建议

1. 优先补齐云存储 4 驱动与消息配置卡片/闸门，这两项直接影响 checklist §十四/§十五/§三。
2. 将通用 CRUD 资源扩展到储值、积分、供应商对账、渠道结算、会员卡券，补齐后台入口。
3. 在具备 MySQL/Redis/RabbitMQ 的环境执行 T79 备份恢复、T80 宝塔部署、T81 压测，并回填 checklist §十四。
4. 外部资质到位后，按 `docs/deploy-baota.md` 替换微信/支付/短信/推送凭证，完成真机联调。
