package com.dotashowcase.inventoryservice.support.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SteamIdValidator implements ConstraintValidator<SteamIdConstraint, Long> {

    @Override
    public void initialize(SteamIdConstraint steamId) {
    }

    @Override
    public boolean isValid(Long steamId, ConstraintValidatorContext cxt) {
        // 7656119 XXX XXX XXXX
        return steamId != null
                && steamId <= 76561199999999999L
                && steamId >  76561190000000000L;
    }
}
