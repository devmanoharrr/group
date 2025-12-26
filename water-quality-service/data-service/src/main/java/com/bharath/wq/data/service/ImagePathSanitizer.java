package com.bharath.wq.data.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ImagePathSanitizer {
  private static final Set<String> ALLOWED_EXT = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

  private ImagePathSanitizer() {}

  public static List<String> sanitize(List<String> input) {
    if (input == null) {
      return List.of();
    }
    final Set<String> unique = new LinkedHashSet<>();
    for (final String raw : input) {
      if (raw == null) {
        continue;
      }
      final String s = raw.trim();
      if (s.isEmpty()) {
        continue;
      }
      // reject absolute paths / URLs
      if (s.startsWith("/") || s.matches("^[A-Za-z]:\\\\.*") || s.startsWith("http")) {
        continue;
      }
      final Path p = Paths.get(s).normalize();
      final String norm = p.toString().replace('\\', '/');
      // reject traversal or going up
      if (norm.startsWith("../") || norm.contains("/../") || norm.equals("..")) {
        continue;
      }
      // extension check
      final String lower = norm.toLowerCase(Locale.ROOT);
      final int dot = lower.lastIndexOf('.');
      if (dot < 0) {
        continue;
      }
      final String ext = lower.substring(dot);
      if (!ALLOWED_EXT.contains(ext)) {
        continue;
      }
      if (norm.length() > 180) {
        continue;
      }
      unique.add(lower);
      if (unique.size() == 3) {
        break; // hard cap
      }
    }
    return new ArrayList<>(unique);
  }
}
