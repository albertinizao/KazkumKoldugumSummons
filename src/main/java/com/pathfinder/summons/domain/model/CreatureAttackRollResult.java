package com.pathfinder.summons.domain.model;

import java.util.List;

public record CreatureAttackRollResult(
        String instanceId,
        String instanceDisplayName,
        List<SingleAttackRollResult> attackResults,
        String displayText
) {
}
