package com.bharath.wq.data.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for pH range constraint (0-14).
 *
 * <p>Validates that pH values are within the scientifically valid range for water pH measurements.
 */
public class PhRangeValidator implements ConstraintValidator<PhRange, Double> {

  private static final double MIN_PH = 0.0;
  private static final double MAX_PH = 14.0;

  @Override
  public boolean isValid(Double value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Null values are handled by @NotNull if needed
    }
    return value >= MIN_PH && value <= MAX_PH;
  }
}
