import 'package:flutter/material.dart';

import '../api.dart';
import '../theme.dart';

class ServicePage extends StatefulWidget {
  const ServicePage({super.key});

  @override
  State<ServicePage> createState() => _ServicePageState();
}

class _ServicePageState extends State<ServicePage> {
  List<dynamic> _services = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final data = await Api.get('/api/public/services');
      if (mounted) setState(() => _services = data as List? ?? []);
    } catch (_) {
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _book(dynamic service) async {
    final date = TextEditingController(
      text: DateTime.now().add(const Duration(days: 1)).toString().substring(0, 10),
    );
    final contact = TextEditingController();
    final phone = TextEditingController();
    String slot = '09:00-10:00';

    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(22)),
      ),
      builder: (context) => Padding(
        padding: EdgeInsets.only(
          left: 20,
          right: 20,
          top: 20,
          bottom: MediaQuery.of(context).viewInsets.bottom + 24,
        ),
        child: StatefulBuilder(
          builder: (context, setSheetState) => Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                '预约 ${service['name']}',
                style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: date,
                decoration: const InputDecoration(labelText: '预约日期（YYYY-MM-DD）'),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                initialValue: slot,
                decoration: const InputDecoration(labelText: '预约时段'),
                items: const [
                  DropdownMenuItem(value: '09:00-10:00', child: Text('09:00-10:00')),
                  DropdownMenuItem(value: '10:00-11:00', child: Text('10:00-11:00')),
                  DropdownMenuItem(value: '14:00-15:00', child: Text('14:00-15:00')),
                  DropdownMenuItem(value: '15:00-16:00', child: Text('15:00-16:00')),
                  DropdownMenuItem(value: '16:00-17:00', child: Text('16:00-17:00')),
                ],
                onChanged: (value) => setSheetState(() => slot = value ?? slot),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: contact,
                decoration: const InputDecoration(labelText: '联系人'),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: phone,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(labelText: '联系电话'),
              ),
              const SizedBox(height: 20),
              FilledButton(
                onPressed: () => Navigator.of(context).pop(true),
                child: const Text('提交预约'),
              ),
            ],
          ),
        ),
      ),
    );

    if (ok != true) return;
    try {
      await Api.post('/api/mini/appointments', {
        'serviceId': service['id'],
        'appointDate': date.text.trim(),
        'timeSlot': slot,
        'appointType': 'STORE',
        'contact': contact.text.trim(),
        'phone': phone.text.trim(),
        'idempotencyKey': 'flutter_appt_${DateTime.now().millisecondsSinceEpoch}',
      });
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('预约成功，可在会员中心查看')),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(e.toString())));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('服务项目')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: const EdgeInsets.all(16),
              children: _services.isEmpty
                  ? const [
                      Padding(
                        padding: EdgeInsets.only(top: 80),
                        child: Center(
                          child: Text('暂无服务项目',
                              style: TextStyle(color: kMuted)),
                        ),
                      )
                    ]
                  : _services
                      .map(
                        (item) => Card(
                          margin: const EdgeInsets.only(bottom: 12),
                          child: Padding(
                            padding: const EdgeInsets.all(16),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Expanded(
                                      child: Text(
                                        item['name']?.toString() ?? '',
                                        style: const TextStyle(
                                          fontSize: 16,
                                          fontWeight: FontWeight.w600,
                                        ),
                                      ),
                                    ),
                                    Text(
                                      '¥${item['price'] ?? 0}',
                                      style: const TextStyle(
                                        color: kPrimary,
                                        fontWeight: FontWeight.w700,
                                        fontSize: 17,
                                      ),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 6),
                                Text(
                                  '${item['category'] ?? '母婴护理'} · ${item['duration'] ?? 60} 分钟 · ${item['ageRange'] ?? '全年龄'}',
                                  style: const TextStyle(
                                      color: kMuted, fontSize: 13),
                                ),
                                const SizedBox(height: 10),
                                Text(
                                  item['description']?.toString() ??
                                      '专业护理团队提供标准化服务，支持到店与上门。',
                                  style: const TextStyle(
                                      color: Color(0xFF56637A), height: 1.6),
                                ),
                                const SizedBox(height: 14),
                                SizedBox(
                                  width: double.infinity,
                                  child: FilledButton(
                                    onPressed: () => _book(item),
                                    child: const Text('立即预约'),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      )
                      .toList(),
            ),
    );
  }
}
