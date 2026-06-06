package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ActiveSummonInstance {
    String id;
    String groupId;
    int index;
    String displayName;
    int maxHitPoints;
    int currentHitPoints;
    ActiveSummonStatus status;

    public static ActiveSummonInstance create(String id, String groupId, int index, String displayName, int maxHitPoints) {
        return ActiveSummonInstance.builder()
                .id(id)
                .groupId(groupId)
                .index(index)
                .displayName(displayName)
                .maxHitPoints(maxHitPoints)
                .currentHitPoints(maxHitPoints)
                .status(ActiveSummonStatus.HEALTHY)
                .build();
    }

    public ActiveSummonInstance damage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage amount must be positive");
        }
        int newHp = this.currentHitPoints - amount;
        return this.toBuilder()
                .currentHitPoints(newHp)
                .status(calculateStatus(newHp, maxHitPoints))
                .build();
    }

    public ActiveSummonInstance heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing amount must be positive");
        }
        int newHp = Math.min(maxHitPoints, this.currentHitPoints + amount);
        return this.toBuilder()
                .currentHitPoints(newHp)
                .status(calculateStatus(newHp, maxHitPoints))
                .build();
    }

    private static ActiveSummonStatus calculateStatus(int current, int max) {
        if (current >= max) {
            return ActiveSummonStatus.HEALTHY;
        } else if (current <= 0) {
            return ActiveSummonStatus.DOWN;
        } else {
            return ActiveSummonStatus.DAMAGED;
        }
    }
}
