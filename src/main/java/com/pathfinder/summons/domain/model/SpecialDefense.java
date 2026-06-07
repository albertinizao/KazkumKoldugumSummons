package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SpecialDefense {
    SpecialDefenseType type;
    String value;
    String notes;
}
