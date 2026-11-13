import 'package:flutter/material.dart';

import '../api.dart';
import '../main.dart';
import '../theme.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  bool _member = true;
  bool _loading = false;
  final _tenant = TextEditingController(text: 'demo');
  final _username = TextEditingController(text: 'nurse');
  final _password = TextEditingController(text: 'admin123');

  Future<void> _login() async {
    setState(() => _loading = true);
    try {
      final code = _tenant.text.trim().isEmpty ? 'demo' : _tenant.text.trim();
      if (_member) {
        final stamp = DateTime.now().millisecondsSinceEpoch;
        final data = await Api.post('/api/mini/auth/login', {
          'code': 'flutter_$stamp',
          'tenantCode': code,
          'unionid': 'unionid_flutter_$stamp',
        }) as Map<String, dynamic>;
        await Api.saveSession(data['token'] as String, code);
      } else {
        final data = await Api.post('/api/tenant/auth/login', {
          'username': _username.text.trim(),
          'password': _password.text,
          'tenantCode': code,
        }) as Map<String, dynamic>;
        await Api.saveSession(data['token'] as String, code);
        await Api.post('/api/auth/me');
      }
      if (!mounted) return;
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const MainShell()),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(e.toString())));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFFFFF1F5), Color(0xFFF5F7FA), Color(0xFFE8F4FF)],
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    width: 78,
                    height: 78,
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [kPrimaryLight, kPrimary],
                      ),
                      borderRadius: BorderRadius.circular(22),
                      boxShadow: [
                        BoxShadow(
                          color: kPrimary.withValues(alpha: 0.3),
                          blurRadius: 24,
                          offset: const Offset(0, 10),
                        ),
                      ],
                    ),
                    child: const Center(
                      child: Text(
                        '悦',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 34,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),
                  const Text('悦宝母婴亲子',
                      style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 8),
                  const Text('科学育儿 · 专业护理 · 多端会员互通',
                      style: TextStyle(color: kMuted)),
                  const SizedBox(height: 32),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(20),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          SegmentedButton<bool>(
                            segments: const [
                              ButtonSegment(value: true, label: Text('会员登录')),
                              ButtonSegment(value: false, label: Text('员工登录')),
                            ],
                            selected: {_member},
                            onSelectionChanged: (value) =>
                                setState(() => _member = value.first),
                          ),
                          const SizedBox(height: 20),
                          TextField(
                            controller: _tenant,
                            decoration: const InputDecoration(labelText: '租户编码'),
                          ),
                          const SizedBox(height: 12),
                          if (!_member) ...[
                            TextField(
                              controller: _username,
                              decoration: const InputDecoration(labelText: '账号'),
                            ),
                            const SizedBox(height: 12),
                            TextField(
                              controller: _password,
                              obscureText: true,
                              decoration: const InputDecoration(labelText: '密码'),
                            ),
                            const SizedBox(height: 12),
                          ],
                          const SizedBox(height: 8),
                          FilledButton(
                            onPressed: _loading ? null : _login,
                            child: _loading
                                ? const SizedBox(
                                    width: 20,
                                    height: 20,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                      color: Colors.white,
                                    ),
                                  )
                                : Text(_member ? '微信一键登录' : '账号登录'),
                          ),
                          const SizedBox(height: 14),
                          const Text(
                            '演示账号：admin / nurse / supplier01 / dist01，密码 admin123',
                            textAlign: TextAlign.center,
                            style: TextStyle(color: kMuted, fontSize: 12),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
