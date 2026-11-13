import 'package:flutter/material.dart';

import '../api.dart';
import '../theme.dart';
import 'service_page.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  Map<String, dynamic> _home = {};
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final data = await Api.get('/api/public/home?terminal=APP');
      if (mounted) setState(() => _home = data as Map<String, dynamic>);
    } catch (_) {
      // 首页允许离线降级展示
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final tenant = _home['tenant'] as Map<String, dynamic>? ?? {};
    final services = (_home['services'] as List?) ?? [];
    final articles = (_home['articles'] as List?) ?? [];

    return Scaffold(
      body: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            Container(
              padding: EdgeInsets.fromLTRB(
                  20, MediaQuery.of(context).padding.top + 24, 20, 28),
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [kPrimaryLight, kPrimary],
                ),
                borderRadius: BorderRadius.only(
                  bottomLeft: Radius.circular(28),
                  bottomRight: Radius.circular(28),
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    tenant['name']?.toString() ?? '悦宝母婴亲子',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 26,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text('专业护理 · 科学育儿 · 亲子成长',
                      style: TextStyle(color: Colors.white70)),
                  const SizedBox(height: 18),
                  Wrap(
                    spacing: 8,
                    children: const [
                      _Tag('持证护理师'),
                      _Tag('到店/上门'),
                      _Tag('会员储值'),
                    ],
                  ),
                ],
              ),
            ),
            if (_loading)
              const Padding(
                padding: EdgeInsets.all(40),
                child: Center(child: CircularProgressIndicator()),
              ),
            Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: _QuickAction(
                          icon: Icons.calendar_month,
                          label: '在线预约',
                          color: const Color(0xFFFFE3EC),
                          onTap: () => Navigator.of(context).push(
                            MaterialPageRoute(
                                builder: (_) => const ServicePage()),
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: _QuickAction(
                          icon: Icons.card_giftcard,
                          label: '亲子活动',
                          color: const Color(0xFFE3F0FF),
                          onTap: _showActivities,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: _QuickAction(
                          icon: Icons.local_mall,
                          label: '母婴商城',
                          color: const Color(0xFFFFF3DC),
                          onTap: () {},
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: _QuickAction(
                          icon: Icons.support_agent,
                          label: '联系客服',
                          color: const Color(0xFFE8F8F0),
                          onTap: () => _toast('客服热线：400-800-2026'),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),
                  const Text('热门服务',
                      style:
                          TextStyle(fontSize: 17, fontWeight: FontWeight.w600)),
                  const SizedBox(height: 12),
                  if (services.isEmpty)
                    const _Empty(text: '暂无服务项目')
                  else
                    ...services.take(4).map((item) => _ServiceTile(item: item)),
                  const SizedBox(height: 24),
                  const Text('育儿资讯',
                      style:
                          TextStyle(fontSize: 17, fontWeight: FontWeight.w600)),
                  const SizedBox(height: 12),
                  if (articles.isEmpty)
                    const _Empty(text: '暂无资讯')
                  else
                    ...articles.take(5).map(
                          (item) => Card(
                            margin: const EdgeInsets.only(bottom: 10),
                            child: ListTile(
                              title: Text(item['title']?.toString() ?? ''),
                              subtitle: Text(
                                fmtTime(item['publishedAt']),
                                style: const TextStyle(color: kMuted),
                              ),
                              trailing: const Icon(Icons.chevron_right),
                              onTap: () => _toast('资讯详情页由 CMS 富文本渲染'),
                            ),
                          ),
                        ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _toast(String text) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
  }

  void _showActivities() {
    final activities = (_home['activities'] as List?) ?? [];
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(22)),
      ),
      builder: (_) => Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('亲子活动',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
            const SizedBox(height: 12),
            if (activities.isEmpty)
              const _Empty(text: '暂无活动')
            else
              ...activities.map(
                (item) => ListTile(
                  contentPadding: EdgeInsets.zero,
                  title: Text(item['title']?.toString() ?? ''),
                  subtitle: Text(
                    '${fmtTime(item['startAt'])} · ${item['address'] ?? ''}',
                    style: const TextStyle(color: kMuted),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _Tag extends StatelessWidget {
  const _Tag(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.2),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(text, style: const TextStyle(color: Colors.white, fontSize: 12)),
    );
  }
}

class _QuickAction extends StatelessWidget {
  const _QuickAction({
    required this.icon,
    required this.label,
    required this.color,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: color,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(icon, color: kPrimary, size: 22),
              ),
              const SizedBox(width: 12),
              Text(label, style: const TextStyle(fontWeight: FontWeight.w500)),
            ],
          ),
        ),
      ),
    );
  }
}

class _ServiceTile extends StatelessWidget {
  const _ServiceTile({required this.item});
  final dynamic item;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: const Color(0xFFFFF1F5),
          child: Text(
            (item['name']?.toString() ?? '服').substring(0, 1),
            style: const TextStyle(color: kPrimary, fontWeight: FontWeight.w600),
          ),
        ),
        title: Text(item['name']?.toString() ?? ''),
        subtitle: Text(
          '${item['category'] ?? '母婴护理'} · ${item['duration'] ?? 60} 分钟',
          style: const TextStyle(color: kMuted),
        ),
        trailing: Text(
          '¥${item['price'] ?? 0}',
          style: const TextStyle(
            color: kPrimary,
            fontWeight: FontWeight.w600,
            fontSize: 15,
          ),
        ),
      ),
    );
  }
}

class _Empty extends StatelessWidget {
  const _Empty({required this.text});
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 28),
      child: Center(
        child: Text(text, style: const TextStyle(color: kMuted)),
      ),
    );
  }
}
