package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AppliedRule {
    FixedRuleType type;
    String description;
}
