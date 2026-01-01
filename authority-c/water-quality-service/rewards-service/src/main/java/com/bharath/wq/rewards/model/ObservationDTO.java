package com.bharath.wq.rewards.model;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record ObservationDTO(
    String id,
    String citizenId,
    String postcode,
    Double temperatureC,
    Double pH,
    Double alkalinityMgL,
    Double turbidityNTU,
    Set<String> observations,
    List<String> imagePaths,
    String authority,
    Instant createdAt) {}
