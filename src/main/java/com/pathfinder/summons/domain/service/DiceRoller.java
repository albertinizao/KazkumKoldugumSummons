package com.pathfinder.summons.domain.service;

import com.pathfinder.summons.domain.model.DiceRoll;

public interface DiceRoller {
    DiceRoll roll(String formula);
    DiceRoll roll(int diceCount, int dieSize, int modifier);
}
