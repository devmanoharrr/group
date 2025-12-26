package com.bharath.wq.data.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for non-negative constraint.
 *
 * <p>Validates that measurement values are non-negative (>= 0).
 */
public class NonNegativeValidator implements ConstraintValidator<NonNegative, Double> {

  @Override
  public boolean isValid(Double value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Null values are handled by @NotNull if needed
    }
    return value >= 0.0;
  }
}
