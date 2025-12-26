package com.bharath.wq.data.repo;

import static org.assertj.core.api.Assertions.assertThat;

import com.bharath.wq.data.DataServiceApplication;
import com.bharath.wq.data.model.ObservationTag;
import com.bharath.wq.data.service.ObservationRecord;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
    classes = DataServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ObservationRepositoryIT {

  private static Path tempDataDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg, @TempDir Path tmp) {
    // Point the service to a fresh DB file under a temp directory
    tempDataDir = tmp.resolve("db");
    reg.add("wq.data.dir", () -> tempDataDir.toAbsolutePath().toString());
  }

  @Autowired private ObservationRepository repo;

  @Test
  void insert_and_query_latest_and_count() {
    var now = Instant.now();

    var r1 =
        new ObservationRecord(
            UUID.randomUUID().toString(),
            "alice",
            "NE1 4LP",
            12.0,
            7.0,
            90.0,
            1.0,
            Set.of(ObservationTag.CLEAR),
            List.of("images/a.jpg"),
            "NE",
            now.minusSeconds(60));

    var r2 =
        new ObservationRecord(
            UUID.randomUUID().toString(),
            "bob",
            "NE2 2AB",
            null,
            null,
            null,
            4.4,
            Set.of(ObservationTag.MURKY),
            List.of("images/b.png"),
            "NE",
            now);

    repo.insert(r1);
    repo.insert(r2);

    assertThat(repo.countByAuthority("NE")).isGreaterThanOrEqualTo(2);

    var latest = repo.findLatest("NE", 5);
    assertThat(latest).isNotEmpty();
    assertThat(latest.get(0).id()).isEqualTo(r2.id()); // newest first
  }
}
