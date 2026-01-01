package com.bharath.wq.data.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;

public class MaxImagesValidator implements ConstraintValidator<MaxImages, Collection<?>> {
  @Override
  public boolean isValid(Collection<?> value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return value.size() <= 3;
  }
}
