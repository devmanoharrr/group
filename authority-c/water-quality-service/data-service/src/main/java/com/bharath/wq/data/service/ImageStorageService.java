package com.bharath.wq.data.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

  private static final Set<String> ALLOWED_MIME =
      Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
  private static final Set<String> ALLOWED_EXT = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

  private final Path imagesRoot;

  public ImageStorageService(@Value("${wq.data.dir:../data}") String dataDir) throws IOException {
    this.imagesRoot = Path.of(dataDir).resolve("images");
    Files.createDirectories(imagesRoot);
  }

  /** Store up to 3 images, return relative paths like "images/uuid.ext". */
  public List<String> store(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return List.of();
    }
    if (files.size() > 3) {
      throw new IllegalArgumentException("No more than 3 images are allowed");
    }

    final List<String> saved = new ArrayList<>();
    for (final MultipartFile f : files) {
      if (f == null || f.isEmpty()) {
        continue;
      }
      final String contentType =
          f.getContentType() == null ? "" : f.getContentType().toLowerCase(Locale.ROOT);
      if (!ALLOWED_MIME.contains(contentType)) {
        throw new IllegalArgumentException("Unsupported image content type: " + contentType);
      }
      final String ext = extensionFromFilename(f.getOriginalFilename());
      if (!ALLOWED_EXT.contains(ext)) {
        throw new IllegalArgumentException("Unsupported image extension: " + ext);
      }

      final String filename = safeName(ext);
      final Path target = imagesRoot.resolve(filename);
      try {
        Files.copy(f.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        throw new IllegalArgumentException("Failed to store image", e);
      }
      saved.add("images/" + filename);
      if (saved.size() == 3) {
        break;
      }
    }
    return saved;
  }

  private static String extensionFromFilename(String name) {
    if (name == null) {
      return "";
    }
    final String lower = name.toLowerCase(Locale.ROOT).trim();
    final int dot = lower.lastIndexOf('.');
    return dot >= 0 ? lower.substring(dot) : "";
  }

  private static String safeName(String ext) {
    return Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ext;
  }
}
