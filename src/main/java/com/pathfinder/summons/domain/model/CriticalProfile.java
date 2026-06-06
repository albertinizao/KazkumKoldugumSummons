package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CriticalProfile {
    int threatRangeStart;
    int multiplier;
}
