package com.bharath.wq.data.api.validation;

import com.bharath.wq.data.api.dto.CreateObservationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOneReadingOrObservationValidator
    implements ConstraintValidator<AtLeastOneReadingOrObservation, CreateObservationRequest> {

  @Override
  public boolean isValid(CreateObservationRequest req, ConstraintValidatorContext ctx) {
    if (req == null) {
      return true;
    }
    final boolean hasMeasurement =
        req.temperatureC() != null
            || req.pH() != null
            || req.alkalinityMgL() != null
            || req.turbidityNTU() != null;
    final boolean hasObservation = req.observations() != null && !req.observations().isEmpty();
    return hasMeasurement || hasObservation;
  }
}
