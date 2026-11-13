# 开放 API 使用手册

> 适用：第三方开发者/合作系统　对应任务：T74 / T75 / T76

## 1. 创建应用

1. 租户管理员登录后台 →「开放平台 / 应用管理」。
2. 新增应用：名称、描述、授权范围、API 配额。
3. 保存后系统生成：
   - `AppKey`（公开标识）
   - `AppSecret`（仅创建时可见，务必妥善保存）
4. 停用应用后，所有签名请求返回 `401`。

> AppSecret 加密存储、界面掩码；如泄露立即重置。

## 2. 签名

```
sign = SHA256(appKey + timestamp + nonce + appSecret)
```

请求头：

| Header | 示例 | 说明 |
| --- | --- | --- |
| `X-App-Key` | `ak_xxx` | 应用标识 |
| `X-Timestamp` | `1763000000000` | 毫秒时间戳，±5 分钟 |
| `X-Nonce` | `8f3c...` | 随机串，建议 UUID |
| `X-Sign` | `a1b2...` | 64 位小写十六进制 |

## 3. 快速调用（Node.js）

```js
import crypto from 'node:crypto'

const base = 'https://api.quantaida.cloud'
const appKey = process.env.APP_KEY
const appSecret = process.env.APP_SECRET

function headers() {
  const timestamp = Date.now().toString()
  const nonce = crypto.randomUUID().replaceAll('-', '')
  const sign = crypto.createHash('sha256').update(appKey + timestamp + nonce + appSecret).digest('hex')
  return { 'X-App-Key': appKey, 'X-Timestamp': timestamp, 'X-Nonce': nonce, 'X-Sign': sign }
}

const res = await fetch(`${base}/api/open/products`, { headers: headers() })
console.log(await res.json())
```

## 4. 接口

| 方法 | 路径 | 说明 | 授权范围 |
| --- | --- | --- | --- |
| GET | `/api/open/products` | 商品列表 | `product.read` |
| GET | `/api/open/orders/{orderNo}` | 订单详情 | `order.read` |
| GET | `/api/open/members/{phone}` | 会员查询 | `member.read` |
| GET | `/api/open/error-codes` | 错误码 | 公开 |

### 示例响应

```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    { "id": 1301, "name": "有机婴幼儿米粉", "category": "FOOD", "status": "ACTIVE" }
  ]
}
```

## 5. 配额与限流

- 应用维度 `apiQuota`，租户维度 `apiQuota`。
- 每次请求计数，超限返回 `40205`。
- 建议客户端本地缓存商品/门店等低频数据。
- 生产环境启用 Redis 做 nonce 防重放与时间窗限流。

## 6. 错误码

| code | 含义 | 处理 |
| --- | --- | --- |
| `0` | 成功 | — |
| `401` | 缺少签名/时间戳过期/签名错误/应用停用 | 校验参数与 Secret，校准时间 |
| `40205` | API 配额超限 | 升级套餐或减少调用 |
| `403` | 租户停用/越权 | 联系租户管理员 |
| `404` | 资源不存在 | 检查订单号/手机号 |
| `429` | 限流/重复提交 | 退避重试 |
| `500` | 服务端异常 | 联系平台运维并提供 eventId |

## 7. 沙箱

- 本地 `h2` profile 即沙箱，演示租户 `demo`。
- 沙箱不调用真实微信/支付/短信。
- 生产前用独立 AppKey，避免与演示数据混用。
- 沙箱与生产数据隔离，禁止将生产 AppSecret 写入客户端代码。

## 8. 安全建议

1. AppSecret 只保存在服务端，禁止放入小程序/前端/App。
2. 使用 HTTPS，禁止明文传输。
3. 时间戳偏差控制在 5 分钟内，服务端启用 nonce 缓存。
4. 对 4xx/5xx 做指数退避，避免雪崩。
5. 记录请求日志（脱敏），便于排查。
6. 定期轮换 Secret，离职/合作终止立即停用应用。

## 9. 常见问题

| 问题 | 处理 |
| --- | --- |
| 401 签名失败 | 拼接顺序是否为 appKey+timestamp+nonce+secret；是否 SHA256 小写 |
| 401 签名过期 | 校准服务器时间，使用毫秒时间戳 |
| 40205 | 查看应用/租户配额，升级或降频 |
| 404 会员不存在 | 手机号需与租户内会员一致 |
| 返回空商品 | 商品未上架或租户不匹配 |
| 回调/异步通知 | 当前版本以查询为主，异步事件通过 RabbitMQ 内部消费 |
