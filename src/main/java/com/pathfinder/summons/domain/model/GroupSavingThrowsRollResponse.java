package com.pathfinder.summons.domain.model;

public record GroupSavingThrowsRollResponse(
        GroupSavingThrowsRollResult rollResult,
        CombatState combatState
) {
}
