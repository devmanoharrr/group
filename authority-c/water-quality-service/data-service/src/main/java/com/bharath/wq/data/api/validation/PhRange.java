package com.bharath.wq.data.api.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Validates that pH values are within the valid range (0-14).
 *
 * <p>This annotation can be applied to Double fields representing pH measurements.
 */
@Documented
@Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PhRangeValidator.class)
public @interface PhRange {
  String message() default "pH value must be between 0 and 14";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
