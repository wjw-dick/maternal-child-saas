import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

/// Android 模拟器访问宿主机使用 10.0.2.2；真机请通过 --dart-define=API_BASE=https://demo.quantaida.cloud 覆盖。
const String kApiBase = String.fromEnvironment(
  'API_BASE',
  defaultValue: 'http://10.0.2.2:18080',
);

class ApiException implements Exception {
  ApiException(this.message, {this.code});
  final String message;
  final int? code;

  @override
  String toString() => message;
}

class Api {
  static String? token;
  static String tenantCode = 'demo';

  static Future<void> restore() async {
    final prefs = await SharedPreferences.getInstance();
    token = prefs.getString('token');
    tenantCode = prefs.getString('tenantCode') ?? 'demo';
  }

  static Future<void> saveSession(String newToken, String code) async {
    token = newToken;
    tenantCode = code;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('token', newToken);
    await prefs.setString('tenantCode', code);
  }

  static Future<void> logout() async {
    token = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('token');
    await prefs.remove('user');
  }

  static Future<dynamic> get(String path) => _send('GET', path);
  static Future<dynamic> post(String path, [Map<String, dynamic>? body]) =>
      _send('POST', path, body);

  static Future<dynamic> _send(
    String method,
    String path, [
    Map<String, dynamic>? body,
  ]) async {
    final uri = Uri.parse('$kApiBase$path');
    final headers = <String, String>{
      'Content-Type': 'application/json',
      'X-Tenant-Code': tenantCode,
      if (token != null) 'Authorization': 'Bearer $token',
    };
    late http.Response response;
    try {
      response = method == 'GET'
          ? await http.get(uri, headers: headers)
          : await http.post(uri,
              headers: headers, body: jsonEncode(body ?? <String, dynamic>{}));
    } catch (e) {
      throw ApiException('网络异常，请检查服务地址与网络连接');
    }

    Map<String, dynamic> payload;
    try {
      payload = jsonDecode(utf8.decode(response.bodyBytes)) as Map<String, dynamic>;
    } catch (_) {
      throw ApiException('服务响应格式异常（${response.statusCode}）');
    }
    final code = payload['code'];
    if (code is int && code == 0) {
      return payload['data'];
    }
    final message = payload['msg']?.toString() ?? '请求失败';
    if (code == 401) {
      await logout();
    }
    throw ApiException(message, code: code is int ? code : null);
  }
}
