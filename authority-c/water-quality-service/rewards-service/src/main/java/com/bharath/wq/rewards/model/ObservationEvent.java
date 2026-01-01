package com.bharath.wq.rewards.model;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public record ObservationEvent(
    @NotBlank String id,
    @NotBlank String citizenId,
    String authority,
    Double temperatureC,
    Double pH,
    Double alkalinityMgL,
    Double turbidityNTU,
    Set<String> observations,
    List<String> imagePaths,
    Instant createdAt) {}
