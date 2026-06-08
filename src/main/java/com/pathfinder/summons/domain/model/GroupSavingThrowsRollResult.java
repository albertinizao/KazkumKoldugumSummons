package com.pathfinder.summons.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record GroupSavingThrowsRollResult(
        String id,
        RollDisplayType type,
        String title,
        String groupId,
        String creatureName,
        LocalDateTime createdAt,
        List<CreatureSavingThrowsRollResult> instanceResults,
        String displayText
) {
}
