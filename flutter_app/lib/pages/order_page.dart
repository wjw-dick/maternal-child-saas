import 'package:flutter/material.dart';

import '../api.dart';
import '../theme.dart';

class OrderPage extends StatefulWidget {
  const OrderPage({super.key});

  @override
  State<OrderPage> createState() => _OrderPageState();
}

class _OrderPageState extends State<OrderPage> {
  List<Map<String, dynamic>> _orders = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final data = await Api.get('/api/mini/orders/mine') as List<dynamic>;
      if (mounted) {
        setState(() {
          _orders = data.cast<Map<String, dynamic>>();
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _loading = false);
        _toast(e.toString());
      }
    }
  }

  Future<void> _pay(Map<String, dynamic> order) async {
    try {
      await Api.post('/api/mini/orders/${order['id']}/pay?channel=MOCK');
      _toast('支付成功');
      _load();
    } catch (e) {
      _toast(e.toString());
    }
  }

  Future<void> _aftersale(Map<String, dynamic> order) async {
    final controller = TextEditingController(text: '商品与描述不符');
    final reason = await showDialog<String>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('申请售后'),
        content: TextField(
          controller: controller,
          maxLines: 3,
          decoration: const InputDecoration(labelText: '售后原因'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(controller.text),
            child: const Text('提交'),
          ),
        ],
      ),
    );
    if (reason == null || reason.trim().isEmpty) return;
    try {
      await Api.post('/api/mini/orders/${order['id']}/aftersale', {'reason': reason.trim()});
      _toast('售后申请已提交，等待门店处理');
      _load();
    } catch (e) {
      _toast(e.toString());
    }
  }

  void _toast(String text) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
  }

  Color _statusColor(String status) {
    if (status == 'COMPLETED') return kPrimary;
    if (status == 'CANCELLED') return kMuted;
    return const Color(0xFFFF8A65);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('我的订单')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _orders.isEmpty
              ? const Center(
                  child: Text('暂无订单', style: TextStyle(color: kMuted)),
                )
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.separated(
                    padding: const EdgeInsets.all(16),
                    itemCount: _orders.length,
                    separatorBuilder: (_, _) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final item = _orders[index];
                      final paid = item['payStatus'] == 'PAID';
                      final status = item['status']?.toString() ?? 'PENDING';
                      return Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text(
                                    item['orderNo']?.toString() ?? '',
                                    style: const TextStyle(fontWeight: FontWeight.w600),
                                  ),
                                  Chip(
                                    label: Text(status),
                                    labelStyle: TextStyle(color: _statusColor(status), fontSize: 12),
                                    backgroundColor: const Color(0xFFFFF1F5),
                                    side: BorderSide.none,
                                  ),
                                ],
                              ),
                              const SizedBox(height: 8),
                              Text(
                                '${item['orderType'] ?? 'ORDER'} · ${item['createdAt'] ?? ''}',
                                style: const TextStyle(color: kMuted, fontSize: 12),
                              ),
                              const SizedBox(height: 12),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text(
                                    '¥${item['payAmount'] ?? 0}',
                                    style: const TextStyle(
                                      color: kPrimary,
                                      fontSize: 18,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  Row(
                                    children: [
                                      if (!paid)
                                        FilledButton(
                                          onPressed: () => _pay(item),
                                          child: const Text('去支付'),
                                        ),
                                      if (paid) ...[
                                        OutlinedButton(
                                          onPressed: () => _aftersale(item),
                                          child: const Text('售后'),
                                        ),
                                      ],
                                    ],
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                ),
    );
  }
}
