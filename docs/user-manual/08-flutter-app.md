# Flutter App 使用与构建手册

> 目录：`flutter_app/`　平台：iOS + Android　适用：会员/家长

## 1. 功能概览

底部 5 个 tab：首页、服务、商品、会员中心、我的。

- 首页：Banner、快捷入口、推荐服务/商品、门店、公告。
- 服务：服务列表、详情、预约弹窗（宝宝/日期/时段/门店/护理师）。
- 商品：商品列表、详情、SKU。
- 会员中心：余额、积分、卡券、宝宝档案、成长记录、订单、预约。
- 我的：个人资料、消息、协议、多语言、修改密码、退出。

登录支持账号密码与 UnionID/微信登录（真实微信 SDK 需运营方配置 AppID/Universal Links）。

## 2. 本地运行

```bash
cd flutter_app
flutter pub get
flutter run
```

后端地址配置：

- Android 模拟器访问宿主机：`http://10.0.2.2:8080`
- iOS 模拟器：`http://127.0.0.1:8080`
- 真机：使用局域网 IP 或 HTTPS 域名

如后端端口为 18080，同步修改 `lib/core/api.dart` 中的 baseUrl。

## 3. 构建 Android

```bash
flutter build apk --release
# 或上架 Google Play
flutter build appbundle --release
```

- 需配置签名：`android/key.properties` + keystore。
- 产物：`build/app/outputs/flutter-apk/app-release.apk` / `build/app/outputs/bundle/release/app-release.aab`。
- 上架需 Google Play 开发者账号（运营方提供）。

## 4. 构建 iOS

```bash
flutter build ios --release
# 或
flutter build ipa --release
```

- 需 macOS + Xcode + Apple Developer 账号。
- 配置 Bundle ID、证书、Provisioning Profile、推送能力。
- 产物：`build/ios/ipa/*.ipa`。
- 上架 App Store 需开发者账号与审核材料（运营方提供）。

## 5. 推送

- Android：接入厂商推送（华为/小米/OPPO/vivo）或 FCM。
- iOS：APNs 证书/密钥。
- 未接入时使用微信订阅消息/站内信兜底。
- 推送 token 由后端保存并按会员/租户发送。

## 6. 多端数据互通

- App 与小程序/公众号/H5 共用 UnionID、会员、订单、积分、宝宝档案。
- 登录后拉取 `/api/mini/member/center`、订单、预约。
- 底部导航名称由后台「菜单可视化 → App 底部」配置。
- 禁止建立独立会员体系，避免数据孤岛。

## 7. 测试

```bash
flutter analyze
flutter test
```

建议覆盖：登录、首页加载、预约提交、订单列表、会员中心、修改密码、断网重试。

## 8. 常见问题

| 问题 | 处理 |
| --- | --- |
| 请求失败 | 检查 baseUrl、模拟器 10.0.2.2、HTTPS 证书 |
| 登录后无数据 | 检查租户编码/UnionID 绑定 |
| 图片不显示 | 检查 `/files` 地址与对象存储 |
| iOS 构建失败 | 检查 CocoaPods、签名、Xcode 版本 |
| 推送收不到 | 检查厂商通道/APNs 配置与 token |
| 上架被拒 | 检查隐私政策、权限说明、账号删除入口 |
