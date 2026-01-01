package com.bharath.wq.data.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PostcodeValidator implements ConstraintValidator<Postcode, String> {
  // Lightweight UK-ish pattern: letters+digits, optional space, letters+digits (not exhaustive)
  private static final Pattern P =
      Pattern.compile("^[A-Za-z]{1,2}\\d[A-Za-z\\d]?\\s?\\d[A-Za-z]{2}$");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return false;
    }
    final String trimmed = value.trim();
    return !trimmed.isEmpty() && P.matcher(trimmed.toUpperCase()).matches();
  }
}
