package com.bharath.wq.data.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ImagePathSanitizerTest {

  @Test
  void sanitize_shouldDropUnsafeAndKeepUpToThree() {
    var out =
        ImagePathSanitizer.sanitize(
            List.of(
                "/etc/passwd",
                "images/a.jpg",
                "http://evil/x.png",
                "../b.png",
                "images/c.webp",
                "images/d.gif",
                "images/e.txt" // bad ext
                ));
    assertThat(out).containsExactly("images/a.jpg", "images/c.webp", "images/d.gif");
  }

  @Test
  void sanitize_shouldDedupAndNormalize() {
    var out =
        ImagePathSanitizer.sanitize(List.of("images/./a.jpg", "images/a.jpg", "IMAGES/A.JPG"));
    assertThat(out).hasSize(1);
    assertThat(out.get(0)).isEqualTo("images/a.jpg");
  }

  @Test
  void sanitize_emptyOrNullIsSafe() {
    assertThat(ImagePathSanitizer.sanitize(null)).isEmpty();
    assertThat(ImagePathSanitizer.sanitize(List.of())).isEmpty();
  }
}
