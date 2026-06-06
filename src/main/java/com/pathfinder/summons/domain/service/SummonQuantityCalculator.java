package com.pathfinder.summons.domain.service;

public interface SummonQuantityCalculator {
    int calculateQuantity(int creatureSummonLevel, int maxSummonLevel);
    String getQuantityFormula(int creatureSummonLevel, int maxSummonLevel);
}
