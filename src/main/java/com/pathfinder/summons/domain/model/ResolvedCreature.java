package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class ResolvedCreature {
    String id;
    String baseTemplateId;
    String displayName;
    int summonLevel;
    String challengeRating;
    SummonTemplateType appliedTemplate; // Nullable
    Alignment alignment;
    CreatureSize size;
    String creatureType;
    List<String> subtypes;
    int initiative;
    List<String> senses;
    int perception;
    ArmorClass armorClass;
    int maxHitPoints;
    String hitPointsFormula;
    int cmb;
    int cmd;
    SavingThrows savingThrows;
    List<Speed> speeds;
    String speedsText;
    List<Attack> attacks;
    String attacksText;
    String space;
    String reach;
    List<String> specialAttacks;
    List<SpecialDefense> specialDefenses;
    List<AbilitySummary> shortAbilities;
    List<AbilityDetail> expandedAbilities;
    String fullStatBlock;
    List<AppliedRule> appliedRules;
}
