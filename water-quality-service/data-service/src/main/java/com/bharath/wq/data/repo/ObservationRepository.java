package com.bharath.wq.data.repo;

import com.bharath.wq.data.model.ObservationTag;
import com.bharath.wq.data.service.ObservationRecord;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ObservationRepository {

  private final JdbcTemplate jdbc;

  public ObservationRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void insert(ObservationRecord r) {
    final String sql =
        "INSERT INTO observation "
            + "(id,citizen_id,postcode,temperature_c,ph,alkalinity_mg_l,turbidity_ntu,"
            + "observations,image_paths,authority,created_at) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    jdbc.update(
        sql,
        r.id(),
        r.citizenId(),
        r.postcode(),
        r.temperatureC(),
        r.pH(),
        r.alkalinityMgL(),
        r.turbidityNTU(),
        toCsv(r.observations()),
        toCsv(r.imagePaths()),
        r.authority(),
        r.createdAt().toString());
  }

  public List<ObservationRecord> findLatest(String authority, int limit) {
    final String base = "SELECT * FROM observation";
    final String order = " ORDER BY created_at DESC LIMIT ?";
    if (authority == null || authority.isBlank()) {
      return jdbc.query(base + order, mapper(), limit);
    }
    return jdbc.query(base + " WHERE authority = ?" + order, mapper(), authority, limit);
  }

  public long countByAuthority(String authority) {
    if (authority == null || authority.isBlank()) {
      return jdbc.queryForObject("SELECT COUNT(*) FROM observation", Long.class);
    }
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM observation WHERE authority = ?", Long.class, authority);
  }

  /**
   * Find an observation by its ID.
   *
   * @param id the observation ID
   * @return the observation record, or null if not found
   */
  public ObservationRecord findById(String id) {
    final List<ObservationRecord> results =
        jdbc.query("SELECT * FROM observation WHERE id = ?", mapper(), id);
    return results.isEmpty() ? null : results.get(0);
  }

  private RowMapper<ObservationRecord> mapper() {
    return new RowMapper<>() {
      @Override
      public ObservationRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ObservationRecord(
            rs.getString("id"),
            rs.getString("citizen_id"),
            rs.getString("postcode"),
            getDouble(rs, "temperature_c"),
            getDouble(rs, "ph"),
            getDouble(rs, "alkalinity_mg_l"),
            getDouble(rs, "turbidity_ntu"),
            toTagSet(rs.getString("observations")),
            toList(rs.getString("image_paths")),
            rs.getString("authority"),
            Instant.parse(rs.getString("created_at")));
      }
    };
  }

  private static Double getDouble(ResultSet rs, String col) throws SQLException {
    final double v = rs.getDouble(col);
    if (rs.wasNull()) {
      return null;
    }
    return v;
  }

  private static String toCsv(Collection<?> items) {
    if (items == null || items.isEmpty()) {
      return null;
    }
    return items.stream().map(Object::toString).collect(Collectors.joining(","));
  }

  private static Set<ObservationTag> toTagSet(String csv) {
    if (csv == null || csv.isBlank()) {
      return Collections.emptySet();
    }
    final String[] parts = csv.split(",");
    final Set<ObservationTag> set = new LinkedHashSet<>();
    for (final String p : parts) {
      set.add(ObservationTag.valueOf(p.trim()));
    }
    return set;
  }

  private static List<String> toList(String csv) {
    if (csv == null || csv.isBlank()) {
      return List.of();
    }
    final String[] parts = csv.split(",");
    final List<String> out = new ArrayList<>(parts.length);
    for (final String p : parts) {
      final String s = p.trim();
      if (!s.isEmpty()) {
        out.add(s);
      }
    }
    return out;
  }
}
