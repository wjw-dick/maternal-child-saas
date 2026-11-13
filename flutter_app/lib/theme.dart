import 'package:flutter/material.dart';

const Color kPrimary = Color(0xFFFF5C8A);
const Color kPrimaryLight = Color(0xFFFF8A65);
const Color kBg = Color(0xFFF5F7FA);
const Color kText = Color(0xFF1F2D3D);
const Color kMuted = Color(0xFF8A94A6);

ThemeData buildTheme() {
  final scheme = ColorScheme.fromSeed(
    seedColor: kPrimary,
    primary: kPrimary,
    surface: Colors.white,
  );
  return ThemeData(
    useMaterial3: true,
    colorScheme: scheme,
    scaffoldBackgroundColor: kBg,
    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.white,
      foregroundColor: kText,
      elevation: 0,
      centerTitle: true,
      titleTextStyle: TextStyle(
        color: kText,
        fontSize: 17,
        fontWeight: FontWeight.w600,
      ),
    ),
    cardTheme: CardThemeData(
      color: Colors.white,
      elevation: 0,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: const Color(0xFFF7F9FC),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(14),
        borderSide: BorderSide.none,
      ),
      contentPadding:
          const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: kPrimary,
        minimumSize: const Size.fromHeight(48),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(999),
        ),
      ),
    ),
  );
}

String money(dynamic value) => (value ?? 0).toString();

String fmtTime(dynamic value) {
  if (value == null) return '-';
  return value.toString().replaceFirst('T', ' ').split('.').first;
}
