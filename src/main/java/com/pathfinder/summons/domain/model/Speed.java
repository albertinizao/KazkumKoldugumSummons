package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Speed {
    SpeedType type;
    int valueFeet;
    String maneuverability;
    String notes;
}
