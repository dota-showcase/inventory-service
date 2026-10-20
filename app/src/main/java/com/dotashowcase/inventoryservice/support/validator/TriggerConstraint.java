package com.dotashowcase.inventoryservice.support.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

// on a String - an Operation.Trigger field fails body parsing (400) on an unknown value, before validation (422)
@Documented
@Constraint(validatedBy = TriggerValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TriggerConstraint {

    String message() default "Trigger must be one of auto, manual, resync";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
