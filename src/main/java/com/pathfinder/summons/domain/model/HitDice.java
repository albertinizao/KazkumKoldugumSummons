package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HitDice {
    int count;
    int dieSize;
}
