package com.pathfinder.summons.domain.model;

public record CriticalThreatResult(
        DiceRoll confirmationRoll,
        DamageRollResult criticalDamage
) {
}
