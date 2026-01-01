package com.bharath.wq.data.api.dto;

import com.bharath.wq.data.model.ObservationTag;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/** Read model returned by GET endpoints (and id after POST). */
public record ObservationResponse(
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
