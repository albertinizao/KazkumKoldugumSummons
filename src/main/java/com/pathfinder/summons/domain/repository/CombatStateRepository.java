package com.pathfinder.summons.domain.repository;

import com.pathfinder.summons.domain.model.CombatState;

public interface CombatStateRepository {
    CombatState getCombatState();
    void saveCombatState(CombatState combatState);
    void clear();
}
