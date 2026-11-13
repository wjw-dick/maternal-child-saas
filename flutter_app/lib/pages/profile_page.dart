import 'package:flutter/material.dart';

import '../api.dart';
import '../theme.dart';
import 'login_page.dart';
import 'order_page.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  Map<String, dynamic> _me = {};

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final data = await Api.get('/api/auth/me') as Map<String, dynamic>;
      if (mounted) setState(() => _me = data);
    } catch (_) {}
  }

  Future<void> _logout() async {
    await Api.logout();
    if (!mounted) return;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginPage()),
      (_) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    final user = _me['user'] as Map<String, dynamic>? ?? {};
    final member = _me['member'] as Map<String, dynamic>? ?? {};
    final name = user['nickname']?.toString() ??
        member['name']?.toString() ??
        _me['username']?.toString() ??
        '悦宝用户';
    final type = _me['userType']?.toString() ?? 'MEMBER';

    return Scaffold(
      appBar: AppBar(title: const Text('个人中心')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 32,
                    backgroundColor: const Color(0xFFFFF1F5),
                    child: Text(
                      name.substring(0, 1),
                      style: const TextStyle(
                        color: kPrimary,
                        fontSize: 26,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(name,
                            style: const TextStyle(
                                fontSize: 19, fontWeight: FontWeight.w600)),
                        const SizedBox(height: 4),
                        Text(
                          '账号类型：$type · 租户：${Api.tenantCode}',
                          style: const TextStyle(color: kMuted, fontSize: 13),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: Column(
              children: [
                _MenuTile(
                  icon: Icons.receipt_long,
                  title: '我的订单',
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const OrderPage()),
                  ),
                ),
                _MenuTile(
                  icon: Icons.calendar_month,
                  title: '我的预约',
                  onTap: () => _toast('请在底部「会员」页查看预约'),
                ),
                _MenuTile(
                  icon: Icons.child_care,
                  title: '宝宝档案',
                  onTap: () => _toast('宝宝档案由会员中心统一管理'),
                ),
                _MenuTile(
                  icon: Icons.lock_outline,
                  title: '修改密码',
                  onTap: _changePassword,
                ),
                _MenuTile(
                  icon: Icons.work_outline,
                  title: '业务工作台',
                  onTap: () => _toast('护理师/供应商/渠道商请在登录页选择员工登录'),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: Column(
              children: [
                _MenuTile(
                  icon: Icons.privacy_tip_outlined,
                  title: '隐私政策',
                  onTap: () => _about(
                    '隐私政策',
                    '我们仅收集提供服务所必需的信息，手机号与证件号采用 AES 加密存储并在展示时脱敏。',
                  ),
                ),
                _MenuTile(
                  icon: Icons.description_outlined,
                  title: '服务协议',
                  onTap: () => _about(
                    '服务协议',
                    '1. 预约服务须按约定时间到店或上门。\n2. 商品订单支付后按门店履约流程配送。\n3. 已支付订单可申请售后，由门店审核处理。\n4. 会员积分与储值仅限本人使用。',
                  ),
                ),
                _MenuTile(
                  icon: Icons.info_outline,
                  title: '关于平台',
                  onTap: () => _about(
                    '关于悦宝',
                    '母婴亲子数字化多端平台 v1.0.0\nJava 17 + Spring Boot 3 多租户 SaaS\nVue3 后台 / uni-app 多端 / Flutter App',
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
          OutlinedButton(
            onPressed: _logout,
            style: OutlinedButton.styleFrom(
              foregroundColor: kPrimary,
              minimumSize: const Size.fromHeight(48),
              side: const BorderSide(color: kPrimary),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(999),
              ),
            ),
            child: const Text('退出登录'),
          ),
          const SizedBox(height: 20),
          const Center(
            child: Text('悦宝母婴亲子 · v1.0.0',
                style: TextStyle(color: kMuted, fontSize: 12)),
          ),
        ],
      ),
    );
  }

  void _toast(String text) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
  }

  Future<void> _changePassword() async {
    final oldController = TextEditingController();
    final newController = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('修改密码'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: oldController,
              obscureText: true,
              decoration: const InputDecoration(labelText: '原密码'),
            ),
            TextField(
              controller: newController,
              obscureText: true,
              decoration: const InputDecoration(labelText: '新密码（至少 6 位）'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('保存'),
          ),
        ],
      ),
    );
    if (ok != true) return;
    if (newController.text.length < 6) {
      _toast('新密码至少 6 位');
      return;
    }
    try {
      await Api.post('/api/auth/change-password', {
        'oldPassword': oldController.text,
        'newPassword': newController.text,
      });
      _toast('密码已修改，请重新登录');
      await Api.logout();
      if (!mounted) return;
      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => const LoginPage()),
        (_) => false,
      );
    } catch (e) {
      _toast(e.toString());
    }
  }

  void _about(String title, String content) {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: Text(title),
        content: Text(content, style: const TextStyle(height: 1.7)),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('知道了'),
          ),
        ],
      ),
    );
  }
}

class _MenuTile extends StatelessWidget {
  const _MenuTile({
    required this.icon,
    required this.title,
    required this.onTap,
  });
  final IconData icon;
  final String title;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon, color: kPrimary),
      title: Text(title),
      trailing: const Icon(Icons.chevron_right, color: kMuted),
      onTap: onTap,
    );
  }
}
