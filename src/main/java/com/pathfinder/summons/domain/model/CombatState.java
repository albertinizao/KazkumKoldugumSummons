package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class CombatState {
    List<ActiveSummonGroup> activeGroups;
    DailyUses dailyUses;
    SummonerConfiguration configuration;
    RollDisplay lastRollResult; // Nullable
}
