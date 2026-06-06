package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class ActiveSummonGroup {
    String id;
    ResolvedCreature resolvedCreature;
    List<ActiveSummonInstance> instances;
}
