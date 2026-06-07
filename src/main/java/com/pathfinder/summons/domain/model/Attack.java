package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class Attack {
    String id;
    String name;
    int attackBonus;
    AttackAbility attackAbility;
    int quantity;
    AttackType attackType;
    List<DamageComponent> damageComponents;
    CriticalProfile critical;
    List<String> notes;
}
