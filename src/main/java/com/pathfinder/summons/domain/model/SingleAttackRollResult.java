package com.pathfinder.summons.domain.model;

public record SingleAttackRollResult(
        String attackId,
        String attackName,
        Integer attackIndex,
        DiceRoll attackRoll,
        DamageRollResult normalDamage,
        CriticalThreatResult criticalThreat,
        String displayText
) {
}
