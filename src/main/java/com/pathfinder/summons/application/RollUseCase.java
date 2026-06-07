package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.CombatState;

public interface RollUseCase {
    CombatState rollGroupAttacks(String groupId);
    CombatState rollGroupSavingThrows(String groupId);
}
