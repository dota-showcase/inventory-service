package com.dotashowcase.inventoryservice.support.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SteamIdValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SteamIdConstraint {

    String message() default "Steam Id must be in '7656119XXXXXXXXXX' format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
