package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class ConfigurationSummary {
    int maxSummonMonsterLevel;
    List<SummonTemplateType> availableTemplates;
}
