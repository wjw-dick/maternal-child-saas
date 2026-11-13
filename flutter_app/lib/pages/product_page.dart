import 'package:flutter/material.dart';

import '../api.dart';
import '../theme.dart';

class ProductPage extends StatefulWidget {
  const ProductPage({super.key});

  @override
  State<ProductPage> createState() => _ProductPageState();
}

class _ProductPageState extends State<ProductPage> {
  List<dynamic> _products = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final data = await Api.get('/api/public/products');
      if (mounted) setState(() => _products = data as List? ?? []);
    } catch (_) {
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _detail(dynamic product) async {
    Map<String, dynamic> detail = {};
    try {
      detail = await Api.get('/api/public/products/${product['id']}')
          as Map<String, dynamic>;
    } catch (_) {}
    if (!mounted) return;
    final skus = (detail['skus'] as List?) ?? [];
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(22)),
      ),
      builder: (_) => Padding(
        padding: const EdgeInsets.all(22),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              product['name']?.toString() ?? '',
              style: const TextStyle(fontSize: 19, fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 8),
            Text(
              '${product['brand'] ?? '悦宝优选'} · ${product['ageRange'] ?? '全年龄'}',
              style: const TextStyle(color: kMuted),
            ),
            const SizedBox(height: 16),
            Text(
              product['detail']?.toString() ??
                  '母婴严选商品，正品保障，支持门店自提与配送。',
              style: const TextStyle(height: 1.7, color: Color(0xFF56637A)),
            ),
            const SizedBox(height: 16),
            const Text('规格',
                style: TextStyle(fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            if (skus.isEmpty)
              const Text('暂无规格库存', style: TextStyle(color: kMuted))
            else
              ...skus.map(
                (sku) => ListTile(
                  contentPadding: EdgeInsets.zero,
                  title: Text(sku['skuCode']?.toString() ?? '默认规格'),
                  subtitle: Text('库存 ${sku['stock'] ?? 0}',
                      style: const TextStyle(color: kMuted)),
                  trailing: Text(
                    '¥${sku['price'] ?? 0}',
                    style: const TextStyle(
                        color: kPrimary, fontWeight: FontWeight.w600),
                  ),
                ),
              ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: () {
                  Navigator.of(context).pop();
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('下单入口已接入订单服务，请在会员端确认规格')),
                  );
                },
                child: const Text('立即购买'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('母婴商城')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : GridView.builder(
              padding: const EdgeInsets.all(16),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                mainAxisSpacing: 12,
                crossAxisSpacing: 12,
                childAspectRatio: 0.78,
              ),
              itemCount: _products.length,
              itemBuilder: (context, index) {
                final item = _products[index];
                return Card(
                  child: InkWell(
                    borderRadius: BorderRadius.circular(18),
                    onTap: () => _detail(item),
                    child: Padding(
                      padding: const EdgeInsets.all(14),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(
                            child: Container(
                              width: double.infinity,
                              decoration: BoxDecoration(
                                color: const Color(0xFFFFF1F5),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Center(
                                child: Text(
                                  (item['name']?.toString() ?? '商')
                                      .substring(0, 1),
                                  style: const TextStyle(
                                    fontSize: 34,
                                    fontWeight: FontWeight.bold,
                                    color: kPrimary,
                                  ),
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(height: 10),
                          Text(
                            item['name']?.toString() ?? '',
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(fontWeight: FontWeight.w600),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            item['brand']?.toString() ?? '悦宝优选',
                            style: const TextStyle(color: kMuted, fontSize: 12),
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),
    );
  }
}
