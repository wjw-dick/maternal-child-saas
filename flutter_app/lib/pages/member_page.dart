import 'package:flutter/material.dart';

import '../api.dart';
import '../theme.dart';

class MemberPage extends StatefulWidget {
  const MemberPage({super.key});

  @override
  State<MemberPage> createState() => _MemberPageState();
}

class _MemberPageState extends State<MemberPage> {
  Map<String, dynamic> _center = {};
  List<dynamic> _orders = [];
  List<dynamic> _appointments = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      _center = await Api.get('/api/mini/member/center') as Map<String, dynamic>? ?? {};
      _orders = await Api.get('/api/mini/orders/mine') as List? ?? [];
      _appointments = await Api.get('/api/mini/appointments/mine') as List? ?? [];
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final member = _center['member'] as Map<String, dynamic>? ?? {};
    final wallet = _center['wallet'] as Map<String, dynamic>? ?? {};
    final babies = (_center['babies'] as List?) ?? [];
    final coupons = (_center['coupons'] as List?) ?? [];

    return Scaffold(
      appBar: AppBar(title: const Text('会员中心')),
      body: RefreshIndicator(
        onRefresh: _load,
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  if (_error != null)
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(18),
                        child: Column(
                          children: [
                            const Icon(Icons.info_outline,
                                color: kMuted, size: 36),
                            const SizedBox(height: 10),
                            const Text('当前账号不是会员身份',
                                style: TextStyle(fontWeight: FontWeight.w600)),
                            const SizedBox(height: 6),
                            Text(
                              '会员中心仅展示 UnionID 会员数据，员工账号请使用业务工作台。',
                              textAlign: TextAlign.center,
                              style: const TextStyle(color: kMuted),
                            ),
                          ],
                        ),
                      ),
                    )
                  else ...[
                    _MemberHeader(member: member),
                    const SizedBox(height: 14),
                    Row(
                      children: [
                        Expanded(
                          child: _StatCard(
                            label: '储值余额',
                            value: money(wallet['balance']),
                            color: kPrimary,
                          ),
                        ),
                        const SizedBox(width: 10),
                        Expanded(
                          child: _StatCard(
                            label: '会员积分',
                            value: money(member['points']),
                            color: const Color(0xFFE6A23C),
                          ),
                        ),
                        const SizedBox(width: 10),
                        Expanded(
                          child: _StatCard(
                            label: '优惠券',
                            value: '${coupons.length}',
                            color: const Color(0xFF2BA471),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 18),
                    const _SectionTitle('我的宝宝'),
                    if (babies.isEmpty)
                      const _Empty('暂无宝宝档案')
                    else
                      ...babies.map(
                        (baby) => Card(
                          margin: const EdgeInsets.only(bottom: 10),
                          child: ListTile(
                            leading: CircleAvatar(
                              backgroundColor: const Color(0xFFFFF1F5),
                              child: Text(
                                (baby['name']?.toString() ?? '宝').substring(0, 1),
                                style: const TextStyle(color: kPrimary),
                              ),
                            ),
                            title: Text(baby['name']?.toString() ?? ''),
                            subtitle: Text(
                              '${baby['birthday'] ?? '未填写生日'} · ${baby['gender'] == 'M' ? '男宝' : baby['gender'] == 'F' ? '女宝' : '未知'}',
                              style: const TextStyle(color: kMuted),
                            ),
                          ),
                        ),
                      ),
                    const SizedBox(height: 16),
                    const _SectionTitle('我的预约'),
                    if (_appointments.isEmpty)
                      const _Empty('暂无预约')
                    else
                      ..._appointments.take(5).map(
                            (item) => Card(
                              margin: const EdgeInsets.only(bottom: 10),
                              child: ListTile(
                                title: Text(
                                    '${item['appointDate']} ${item['timeSlot']}'),
                                subtitle: Text(
                                  item['appointmentNo']?.toString() ?? '',
                                  style: const TextStyle(color: kMuted),
                                ),
                                trailing: Text(
                                  item['status']?.toString() ?? '',
                                  style: const TextStyle(color: kPrimary),
                                ),
                              ),
                            ),
                          ),
                    const SizedBox(height: 16),
                    const _SectionTitle('我的订单'),
                    if (_orders.isEmpty)
                      const _Empty('暂无订单')
                    else
                      ..._orders.take(5).map(
                            (item) => Card(
                              margin: const EdgeInsets.only(bottom: 10),
                              child: ListTile(
                                title: Text(item['orderNo']?.toString() ?? ''),
                                subtitle: Text(
                                  '${fmtTime(item['createdAt'])} · ${item['payStatus']}',
                                  style: const TextStyle(color: kMuted),
                                ),
                                trailing: Text(
                                  '¥${item['payAmount'] ?? 0}',
                                  style: const TextStyle(
                                    color: kPrimary,
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              ),
                            ),
                          ),
                  ],
                ],
              ),
      ),
    );
  }
}

class _MemberHeader extends StatelessWidget {
  const _MemberHeader({required this.member});
  final Map<String, dynamic> member;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(colors: [kPrimaryLight, kPrimary]),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          CircleAvatar(
            radius: 30,
            backgroundColor: Colors.white.withValues(alpha: 0.25),
            child: Text(
              (member['name']?.toString() ?? '悦').substring(0, 1),
              style: const TextStyle(
                  color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  member['name']?.toString() ?? '悦宝会员',
                  style: const TextStyle(
                      color: Colors.white,
                      fontSize: 20,
                      fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 4),
                Text(
                  '会员编号：${member['memberNo'] ?? '-'} · ${member['level'] ?? 'NORMAL'}',
                  style: const TextStyle(color: Colors.white70, fontSize: 13),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _StatCard extends StatelessWidget {
  const _StatCard({
    required this.label,
    required this.value,
    required this.color,
  });
  final String label;
  final String value;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 16),
        child: Column(
          children: [
            Text(value,
                style: TextStyle(
                    fontSize: 20, fontWeight: FontWeight.w700, color: color)),
            const SizedBox(height: 4),
            Text(label,
                style: const TextStyle(fontSize: 12, color: kMuted)),
          ],
        ),
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10, top: 4),
      child: Text(text,
          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
    );
  }
}

class _Empty extends StatelessWidget {
  const _Empty(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 28),
        child: Center(
          child: Text(text, style: const TextStyle(color: kMuted)),
        ),
      ),
    );
  }
}
