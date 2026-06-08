package com.pathfinder.summons.domain.model;

public record GroupAttackRollResponse(
        GroupAttackRollResult rollResult,
        CombatState combatState
) {
}
