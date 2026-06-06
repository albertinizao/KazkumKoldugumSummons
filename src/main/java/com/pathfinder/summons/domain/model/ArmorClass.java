package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ArmorClass {
    int normal;
    int touch;
    int flatFooted;
    String detail;
}
