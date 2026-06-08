package com.pathfinder.summons.domain.model;

import java.util.List;

public record DamageRollResult(
        List<DamageComponentRollResult> components,
        int total,
        String displayText
) {
}
