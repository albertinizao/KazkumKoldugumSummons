package com.pathfinder.summons.domain.model;

public record DamageComponentRollResult(
        String formula,
        DiceRoll roll,
        DamageType damageType,
        boolean multipliesOnCritical,
        int total
) {
}
