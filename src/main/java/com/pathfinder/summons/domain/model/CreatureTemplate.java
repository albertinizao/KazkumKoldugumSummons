package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class CreatureTemplate {
    String id;
    String name;
    int summonLevel;
    Alignment alignment;
    CreatureSize size;
    String creatureType;
    List<String> subtypes;
    List<SummonTemplateType> allowedTemplates;
    int initiative;
    List<String> senses;
    int perception;
    AbilityScores abilities;
    ArmorClass armorClass;
    HitPointsDefinition hitPoints;
    SavingThrows savingThrows;
    List<Speed> speeds;
    List<Attack> attacks;
    String space;
    String reach;
    List<String> specialAttacks;
    List<SpecialDefense> specialDefenses;
    List<String> tacticalNotes;
    List<AbilitySummary> shortAbilities;
    List<AbilityDetail> expandedAbilities;
    String fullStatBlock;
}
