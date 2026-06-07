package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AbilityScores {
    int strength;
    int dexterity;
    int constitution;
    int intelligence;
    int wisdom;
    int charisma;

    public static int getModifierForScore(int score) {
        return Math.floorDiv(score - 10, 2);
    }

    public int getStrengthModifier() {
        return getModifierForScore(strength);
    }

    public int getDexterityModifier() {
        return getModifierForScore(dexterity);
    }

    public int getConstitutionModifier() {
        return getModifierForScore(constitution);
    }

    public int getIntelligenceModifier() {
        return getModifierForScore(intelligence);
    }

    public int getWisdomModifier() {
        return getModifierForScore(wisdom);
    }

    public int getCharismaModifier() {
        return getModifierForScore(charisma);
    }
}
