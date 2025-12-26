package com.bharath.wq.data.api.dto;

import com.bharath.wq.data.api.validation.AtLeastOneReadingOrObservation;
import com.bharath.wq.data.api.validation.MaxImages;
import com.bharath.wq.data.api.validation.NonNegative;
import com.bharath.wq.data.api.validation.PhRange;
import com.bharath.wq.data.api.validation.Postcode;
import com.bharath.wq.data.model.ObservationTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

/**
 * JSON payload for creating an observation (images by path for now). File uploads will be supported
 * later via multipart.
 */
@AtLeastOneReadingOrObservation
public record CreateObservationRequest(
    @NotBlank String citizenId,
    @NotBlank @Postcode String postcode,
    @NonNegative Double temperatureC,
    @PhRange Double pH,
    @NonNegative Double alkalinityMgL,
    @NonNegative Double turbidityNTU,
    @Size(max = 10) Set<ObservationTag> observations,
    @MaxImages List<String> imagePaths,
    String authority) {}
