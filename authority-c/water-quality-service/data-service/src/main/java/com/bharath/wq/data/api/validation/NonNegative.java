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
 * Validates that measurement values are non-negative.
 *
 * <p>This annotation can be applied to Double fields representing measurements like temperature,
 * alkalinity, or turbidity that should not be negative.
 */
@Documented
@Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NonNegativeValidator.class)
public @interface NonNegative {
  String message() default "Measurement value must be non-negative";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
