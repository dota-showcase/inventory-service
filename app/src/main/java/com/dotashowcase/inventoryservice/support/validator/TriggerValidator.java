package com.dotashowcase.inventoryservice.support.validator;

import com.dotashowcase.inventoryservice.model.Operation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class TriggerValidator implements ConstraintValidator<TriggerConstraint, String> {

    @Override
    public boolean isValid(String trigger, ConstraintValidatorContext cxt) {
        // null - not passed
        return trigger == null
                || Arrays.stream(Operation.Trigger.values()).anyMatch(value -> value.name().equals(trigger));
    }
}
