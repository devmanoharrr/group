package com.bharath.wq.data.api.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target({TYPE, ANNOTATION_TYPE, RECORD_COMPONENT})
@Retention(RUNTIME)
@Constraint(validatedBy = AtLeastOneReadingOrObservationValidator.class)
public @interface AtLeastOneReadingOrObservation {
  String message() default
      "Provide at least one measurement (temperatureC, pH, alkalinityMgL, turbidityNTU)"
          + " or at least one observation tag";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
