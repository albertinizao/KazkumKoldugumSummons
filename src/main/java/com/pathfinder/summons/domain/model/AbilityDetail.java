package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AbilityDetail {
    String name;
    String text;
}
