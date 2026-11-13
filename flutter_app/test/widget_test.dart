import 'package:flutter_test/flutter_test.dart';
import 'package:mc_app/theme.dart';

void main() {
  test('主题与金额格式化', () {
    expect(buildTheme().colorScheme.primary.toARGB32(), kPrimary.toARGB32());
    expect(money(12.5), '12.5');
    expect(fmtTime('2026-11-13T10:00:00'), '2026-11-13 10:00:00');
  });
}
