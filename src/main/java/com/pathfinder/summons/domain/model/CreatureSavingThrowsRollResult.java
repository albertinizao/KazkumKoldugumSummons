package com.pathfinder.summons.domain.model;

public record CreatureSavingThrowsRollResult(
        String instanceId,
        String instanceDisplayName,
        DiceRoll fortitude,
        DiceRoll reflex,
        DiceRoll will,
        String displayText
) {
}
