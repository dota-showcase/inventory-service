package com.dotashowcase.inventoryservice.support.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SteamIdValidator implements ConstraintValidator<SteamIdConstraint, Long> {

    // individual account - 76561197960265728 + 32-bit account id, 0 is not an account
    private static final long MIN_STEAM_ID = 76561197960265729L;

    private static final long MAX_STEAM_ID = 76561202255233023L;

    @Override
    public void initialize(SteamIdConstraint steamId) {
    }

    @Override
    public boolean isValid(Long steamId, ConstraintValidatorContext cxt) {
        return steamId != null
                && steamId >= MIN_STEAM_ID
                && steamId <= MAX_STEAM_ID;
    }
}
