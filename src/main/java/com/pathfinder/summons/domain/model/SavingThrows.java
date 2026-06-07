package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SavingThrows {
    int fortitude;
    int reflex;
    int will;
    SavingThrowAbility fortitudeAbility;
}
