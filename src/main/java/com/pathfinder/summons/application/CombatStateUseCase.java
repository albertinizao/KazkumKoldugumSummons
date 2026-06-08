package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.CombatState;
import com.pathfinder.summons.domain.model.GroupAttackRollResponse;
import com.pathfinder.summons.domain.model.GroupSavingThrowsRollResponse;
import com.pathfinder.summons.domain.model.SummonTemplateType;

public interface CombatStateUseCase {
    CombatState getCombatState();
    CombatState summon(String creatureId, SummonTemplateType templateType);
    CombatState damageInstance(String groupId, String instanceId, int amount);
    CombatState healInstance(String groupId, String instanceId, int amount);
    CombatState removeInstance(String groupId, String instanceId);
    CombatState clearSummons();
    CombatState updateMaxSummonLevel(int newLevel);
    CombatState updateDailyUses(int maximum, int remaining);
    CombatState clearLastRollResult();
    CombatState incrementDailyUses(int amount);
    CombatState decrementDailyUses(int amount);
    CombatState resetDailyUses();
    GroupAttackRollResponse rollGroupAttacks(String groupId);
    GroupSavingThrowsRollResponse rollGroupSavingThrows(String groupId);
}
