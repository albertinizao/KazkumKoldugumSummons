package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.SummonerConfiguration;
import org.springframework.stereotype.Component;

@Component
public class SummonQuantityCalculator {

    public SummonQuantityPlan describe(SummonerConfiguration configuration, int creatureSummonLevel) {
        int maxSummonMonsterLevel = configuration.getMaxSummonMonsterLevel();
        int levelDifference = maxSummonMonsterLevel - creatureSummonLevel;

        if (levelDifference <= 0) {
            return new SummonQuantityPlan(1, "1");
        }
        if (levelDifference == 1) {
            return new SummonQuantityPlan(3, "1d3");
        }
        return new SummonQuantityPlan(4 + (levelDifference - 1), "1d4+" + (levelDifference - 1));
    }

    public boolean isAvailable(SummonerConfiguration configuration, int creatureSummonLevel) {
        return creatureSummonLevel <= configuration.getMaxSummonMonsterLevel();
    }

    public record SummonQuantityPlan(int maximumPossibleQuantity, String formula) {
    }
}
