package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class CombatState {
    List<ActiveSummonGroup> activeGroups;
    DailyUses dailyUses;
    ConfigurationSummary configuration;
    RollDisplay lastRollResult; // Nullable
    List<SummonShortcut> recentlyUsedSummons;
    List<SummonShortcut> mostUsedSummons;
}
