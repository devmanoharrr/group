package com.bharath.wq.data.service;

import com.bharath.wq.data.model.ObservationTag;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/** Internal persistence model used by the JDBC repository. */
public record ObservationRecord(
    String id,
    String citizenId,
    String postcode,
    Double temperatureC,
    Double pH,
    Double alkalinityMgL,
    Double turbidityNTU,
    Set<ObservationTag> observations,
    List<String> imagePaths,
    String authority,
    Instant createdAt) {}
