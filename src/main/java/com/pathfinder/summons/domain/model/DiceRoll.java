package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class DiceRoll {
    String formula;
    List<Integer> naturalResults;
    int modifier;
    int total;
    String label; // Nullable
}
