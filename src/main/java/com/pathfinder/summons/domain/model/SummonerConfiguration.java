package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.Set;

@Value
@Builder(toBuilder = true)
public class SummonerConfiguration {
    int maxSummonMonsterLevel;
    DailyUses dailyUses;
    Set<SummonTemplateType> availableTemplates;
}
