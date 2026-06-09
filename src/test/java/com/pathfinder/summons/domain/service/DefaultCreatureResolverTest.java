package com.pathfinder.summons.domain.service;

import com.pathfinder.summons.domain.model.AbilityScores;
import com.pathfinder.summons.domain.model.Alignment;
import com.pathfinder.summons.domain.model.ArmorClass;
import com.pathfinder.summons.domain.model.Attack;
import com.pathfinder.summons.domain.model.AttackAbility;
import com.pathfinder.summons.domain.model.AttackType;
import com.pathfinder.summons.domain.model.CriticalProfile;
import com.pathfinder.summons.domain.model.CreatureSize;
import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.DamageAbility;
import com.pathfinder.summons.domain.model.DamageComponent;
import com.pathfinder.summons.domain.model.DamageType;
import com.pathfinder.summons.domain.model.HitDice;
import com.pathfinder.summons.domain.model.HitPointsDefinition;
import com.pathfinder.summons.domain.model.ResolvedCreature;
import com.pathfinder.summons.domain.model.SavingThrowAbility;
import com.pathfinder.summons.domain.model.SavingThrows;
import com.pathfinder.summons.domain.model.Speed;
import com.pathfinder.summons.domain.model.SpeedType;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import com.pathfinder.summons.domain.model.SummonerConfiguration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultCreatureResolverTest {

    private final DefaultCreatureResolver resolver = new DefaultCreatureResolver();

    @Test
    void appliesChthonicTemplateEffectsAndDeepGuardianBonuses() {
        CreatureTemplate template = baseTemplate()
                .id("mole")
                .name("Mole")
                .subtypes(List.of())
                .allowedTemplates(List.of(SummonTemplateType.CHTHONIC))
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(30)
                        .formula("5d8+5")
                        .hitDice(HitDice.builder().count(5).dieSize(8).build())
                        .build())
                .speeds(List.of(speed(SpeedType.LAND, 30)))
                .attacks(List.of(attack("Bite", 3, AttackAbility.STRENGTH, "1d4+1", DamageType.PIERCING)))
                .specialAttacks(List.of())
                .specialDefenses(List.of())
                .build();

        ResolvedCreature resolved = resolver.resolve(template, SummonTemplateType.CHTHONIC, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getAlignment()).isEqualTo(Alignment.NG);
        assertThat(resolved.getSubtypes()).contains("earth");
        assertThat(resolved.getSenses()).contains("darkvision 60 ft.", "tremorsense 60 ft.");
        assertThat(resolved.getSpeeds())
                .extracting(speed -> speed.getType() + ":" + speed.getValueFeet())
                .containsExactly("LAND:30", "BURROW:15");
        assertThat(resolved.getArmorClass().getNormal()).isEqualTo(13);
        assertThat(resolved.getArmorClass().getTouch()).isEqualTo(11);
        assertThat(resolved.getArmorClass().getFlatFooted()).isEqualTo(13);
        assertThat(resolved.getAttacks()).hasSize(1);
        assertThat(resolved.getAttacks().getFirst().getAttackBonus()).isEqualTo(6);
        assertThat(resolved.getAttacks().getFirst().getDamageComponents()).hasSize(2);
        assertThat(resolved.getAttacks().getFirst().getDamageComponents().getLast().getFormula()).isEqualTo("1d6");
        assertThat(resolved.getAttacks().getFirst().getDamageComponents().getLast().getDamageType()).isEqualTo(DamageType.ACID);
        assertThat(resolved.getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains("DAMAGE_REDUCTION:3/—", "RESISTANCE:acid 15");
        assertThat(resolved.getFullStatBlock()).contains("NG Small beast");
    }

    @Test
    void appliesFieryTemplateEffectsWithoutTouchArmorBonus() {
        CreatureTemplate template = baseTemplate()
                .id("hound")
                .name("Hound")
                .allowedTemplates(List.of())
                .speeds(List.of(speed(SpeedType.LAND, 40)))
                .attacks(List.of(attack("Bite", 3, AttackAbility.STRENGTH, "1d6+1", DamageType.PIERCING)))
                .build();

        ResolvedCreature resolved = resolver.resolve(template, SummonTemplateType.FIERY, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getAlignment()).isEqualTo(Alignment.NG);
        assertThat(resolved.getSubtypes()).contains("fire");
        assertThat(resolved.getSenses()).contains("darkvision 60 ft.").doesNotContain("tremorsense 60 ft.");
        assertThat(resolved.getArmorClass().getNormal()).isEqualTo(12);
        assertThat(resolved.getArmorClass().getTouch()).isEqualTo(11);
        assertThat(resolved.getArmorClass().getFlatFooted()).isEqualTo(12);
        assertThat(resolved.getAttacks().getFirst().getDamageComponents())
                .extracting(DamageComponent::getDamageType)
                .containsExactly(DamageType.PIERCING, DamageType.FIRE);
        assertThat(resolved.getAttacks().getFirst().getDamageComponents().getLast().getFormula()).isEqualTo("1");
        assertThat(resolved.getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains("IMMUNITY:fire", "VULNERABILITY:cold")
                .doesNotContain("RESISTANCE:fire 10");
    }

    @Test
    void appliesElementalDamageToTailAttacksAndInfersLogicalPhysicalDamageType() {
        CreatureTemplate template = baseTemplate()
                .id("tail-sweep")
                .name("Tail Sweep Beast")
                .allowedTemplates(List.of())
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(110)
                        .formula("11d8+33")
                        .hitDice(HitDice.builder().count(11).dieSize(8).build())
                        .build())
                .speeds(List.of(speed(SpeedType.LAND, 30)))
                .attacks(List.of(attack("Tail Sweep", 7, AttackAbility.STRENGTH, "1d8+3", DamageType.OTHER)))
                .build();

        ResolvedCreature resolved = resolver.resolve(template, SummonTemplateType.FIERY, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getAttacks()).hasSize(1);
        assertThat(resolved.getAttacks().getFirst().getDamageComponents())
                .extracting(DamageComponent::getDamageType)
                .containsExactly(DamageType.BLUDGEONING, DamageType.FIRE);
        assertThat(resolved.getAttacks().getFirst().getDamageComponents().getLast().getFormula()).isEqualTo("2d6");
    }

    @ParameterizedTest
    @CsvSource({
            "CHTHONIC,1,1,ACID",
            "CHTHONIC,5,1d6,ACID",
            "CHTHONIC,11,2d6,ACID",
            "FIERY,1,1,FIRE",
            "FIERY,5,1d6,FIRE",
            "FIERY,11,2d6,FIRE"
    })
    void appliesTieredTemplateDamageToNaturalAttacksOnly(SummonTemplateType templateType,
                                                         int hitDiceCount,
                                                         String expectedFormula,
                                                         DamageType expectedDamageType) {
        CreatureTemplate template = baseTemplate()
                .id("template-damage-tiered")
                .name("Template Damage Tiered")
                .allowedTemplates(List.of(templateType))
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(10 * hitDiceCount)
                        .formula(hitDiceCount + "d8+5")
                        .hitDice(HitDice.builder().count(hitDiceCount).dieSize(8).build())
                        .build())
                .attacks(List.of(
                        attack("Bite", 3, AttackAbility.STRENGTH, "1d4+1", DamageType.PIERCING),
                        attack("Warhammer", 3, AttackAbility.STRENGTH, "1d8+1", DamageType.BLUDGEONING)))
                .build();

        ResolvedCreature resolved = resolver.resolve(template, templateType, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getAttacks()).hasSize(2);

        assertThat(resolved.getAttacks().get(0).getDamageComponents())
                .hasSize(2);
        assertThat(resolved.getAttacks().get(0).getDamageComponents().getLast().getFormula()).isEqualTo(expectedFormula);
        assertThat(resolved.getAttacks().get(0).getDamageComponents().getLast().getDamageType()).isEqualTo(expectedDamageType);

        assertThat(resolved.getAttacks().get(1).getDamageComponents()).hasSize(1);
        assertThat(resolved.getAttacks().get(1).getDamageComponents().getFirst().getDamageType())
                .isEqualTo(DamageType.BLUDGEONING);
    }

    @Test
    void rejectsTemplatesForOutsidersEvenIfTheTemplateWasListedBefore() {
        CreatureTemplate template = baseTemplate()
                .id("astral-deva")
                .name("Astral Deva")
                .creatureType("outsider")
                .allowedTemplates(List.of(SummonTemplateType.CELESTIAL))
                .build();

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                resolver.resolve(template, SummonTemplateType.CELESTIAL, SummonerConfiguration.defaultConfiguration()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outsider");
    }

    @Test
    void calculatesMaximumHitPointsForAnkylosaurusUsingMaximumPossibleBaseRoll() {
        CreatureTemplate template = baseTemplate()
                .id("ankylosaurus")
                .name("Ankylosaurus")
                .abilities(AbilityScores.builder()
                        .strength(27)
                        .dexterity(10)
                        .constitution(17)
                        .intelligence(2)
                        .wisdom(13)
                        .charisma(8)
                        .build())
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(110)
                        .formula("10d8+30")
                        .hitDice(HitDice.builder().count(10).dieSize(8).build())
                        .build())
                .build();

        ResolvedCreature resolved = resolver.resolve(template, null, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getMaxHitPoints()).isEqualTo(130);
    }

    @Test
    void calculatesMaximumHitPointsForAllosaurusUsingMaximumPossibleBaseRoll() {
        CreatureTemplate template = baseTemplate()
                .id("allosaurus")
                .name("Allosaurus")
                .initiative(5)
                .senses(List.of("low-light vision", "scent"))
                .perception(28)
                .abilities(AbilityScores.builder()
                        .strength(26)
                        .dexterity(13)
                        .constitution(19)
                        .intelligence(2)
                        .wisdom(15)
                        .charisma(10)
                        .build())
                .armorClass(ArmorClass.builder()
                        .normal(19)
                        .touch(9)
                        .flatFooted(18)
                        .detail("+1 Dex, +10 natural, -2 size")
                        .build())
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(132)
                        .formula("11d8+44")
                        .hitDice(HitDice.builder().count(11).dieSize(8).build())
                        .build())
                .savingThrows(SavingThrows.builder()
                        .fortitude(11)
                        .reflex(8)
                        .will(7)
                        .fortitudeAbility(SavingThrowAbility.CONSTITUTION)
                        .build())
                .build();

        ResolvedCreature resolved = resolver.resolve(template, null, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getMaxHitPoints()).isEqualTo(154);
    }

    @Test
    void keepsTemplateDamageComponentsAvailableForCriticalMultiplication() {
        CreatureTemplate template = baseTemplate()
                .id("template-damage")
                .name("Template Damage")
                .allowedTemplates(List.of(SummonTemplateType.CHTHONIC))
                .speeds(List.of(speed(SpeedType.LAND, 30)))
                .attacks(List.of(attack("Bite", 3, AttackAbility.STRENGTH, "1d4+1", DamageType.PIERCING)))
                .build();

        ResolvedCreature resolved = resolver.resolve(template, SummonTemplateType.CHTHONIC, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getAttacks().getFirst().getDamageComponents())
                .last()
                .extracting(DamageComponent::isMultipliesOnCritical)
                .isEqualTo(true);
    }

    @Test
    void addsTemplateSmiteAndListedResistancesForCelestialCreatures() {
        CreatureTemplate template = baseTemplate()
                .id("archon")
                .name("Archon")
                .abilities(AbilityScores.builder()
                        .strength(10)
                        .dexterity(10)
                        .constitution(10)
                        .intelligence(2)
                        .wisdom(10)
                        .charisma(14)
                        .build())
                .allowedTemplates(List.of(SummonTemplateType.CELESTIAL))
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(12)
                        .formula("2d8+2")
                        .hitDice(HitDice.builder().count(2).dieSize(8).build())
                        .build())
                .speeds(List.of(speed(SpeedType.FLY, 60)))
                .attacks(List.of(attack("Aura", 0, AttackAbility.NONE, "—", DamageType.OTHER)))
                .challengeRating("2")
                .fullStatBlock("CR 2\nArchon\n...")
                .build();

        ResolvedCreature resolved = resolver.resolve(template, SummonTemplateType.CELESTIAL, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getAlignment()).isEqualTo(Alignment.NG);
        assertThat(resolved.getSpecialAttacks()).contains("Smite evil 1/day (swift action) — attack +2, damage +2");
        assertThat(resolved.getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains("RESISTANCE:cold 5", "RESISTANCE:acid 5", "RESISTANCE:electricity 5", "SPELL_RESISTANCE:7");
    }

    @Test
    void addsSpellResistanceForEntropicCreatures() {
        CreatureTemplate template = baseTemplate()
                .id("entropic")
                .name("Entropic")
                .allowedTemplates(List.of(SummonTemplateType.ENTROPIC))
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(12)
                        .formula("2d8+2")
                        .hitDice(HitDice.builder().count(2).dieSize(8).build())
                        .build())
                .speeds(List.of(speed(SpeedType.LAND, 60)))
                .attacks(List.of(attack("Aura", 0, AttackAbility.NONE, "—", DamageType.OTHER)))
                .challengeRating("2")
                .fullStatBlock("CR 2\nEntropic\n...")
                .build();

        ResolvedCreature resolved = resolver.resolve(template, SummonTemplateType.ENTROPIC, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains("RESISTANCE:acid 5", "RESISTANCE:fire 5", "SPELL_RESISTANCE:7");
    }

    @Test
    void addsSpellResistanceForResoluteCreatures() {
        CreatureTemplate template = baseTemplate()
                .id("resolute")
                .name("Resolute")
                .allowedTemplates(List.of(SummonTemplateType.RESOLUTE))
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(12)
                        .formula("2d8+2")
                        .hitDice(HitDice.builder().count(2).dieSize(8).build())
                        .build())
                .speeds(List.of(speed(SpeedType.LAND, 60)))
                .attacks(List.of(attack("Aura", 0, AttackAbility.NONE, "—", DamageType.OTHER)))
                .challengeRating("2")
                .fullStatBlock("CR 2\nResolute\n...")
                .build();

        ResolvedCreature resolved = resolver.resolve(template, SummonTemplateType.RESOLUTE, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains("RESISTANCE:acid 5", "RESISTANCE:cold 5", "RESISTANCE:fire 5", "SPELL_RESISTANCE:7");
    }

    @Test
    void scalesTemplateDefensesAndSmiteWithElevenHitDice() {
        CreatureTemplate template = baseTemplate()
                .id("resolute-guardian")
                .name("Resolute Guardian")
                .abilities(AbilityScores.builder()
                        .strength(18)
                        .dexterity(12)
                        .constitution(16)
                        .intelligence(2)
                        .wisdom(12)
                        .charisma(16)
                        .build())
                .allowedTemplates(List.of(SummonTemplateType.RESOLUTE))
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(120)
                        .formula("11d8+32")
                        .hitDice(HitDice.builder().count(11).dieSize(8).build())
                        .build())
                .speeds(List.of(speed(SpeedType.LAND, 40)))
                .attacks(List.of(attack("Claw", 8, AttackAbility.STRENGTH, "1d6+4", DamageType.SLASHING)))
                .challengeRating("11")
                .fullStatBlock("CR 11\nResolute Guardian\n...")
                .build();

        ResolvedCreature resolved = resolver.resolve(template, SummonTemplateType.RESOLUTE, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getSpecialAttacks())
                .contains("Smite chaos 1/day (swift action) — attack +3, damage +11");
        assertThat(resolved.getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains(
                        "RESISTANCE:acid 15",
                        "RESISTANCE:cold 15",
                        "RESISTANCE:fire 15",
                        "SPELL_RESISTANCE:16",
                        "DAMAGE_REDUCTION:10/chaotic");
    }

    @Test
    void grantsDeepGuardianWhenTheBaseCreatureAlreadyHasEarthSubtype() {
        CreatureTemplate template = baseTemplate()
                .id("stone-cat")
                .name("Stone Cat")
                .subtypes(List.of("earth"))
                .allowedTemplates(List.of())
                .speeds(List.of(speed(SpeedType.LAND, 30)))
                .attacks(List.of(attack("Claw", 3, AttackAbility.NONE, "1d4", DamageType.SLASHING)))
                .build();

        ResolvedCreature resolved = resolver.resolve(template, null, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getAlignment()).isEqualTo(Alignment.N);
        assertThat(resolved.getArmorClass().getNormal()).isEqualTo(13);
        assertThat(resolved.getArmorClass().getTouch()).isEqualTo(11);
        assertThat(resolved.getArmorClass().getFlatFooted()).isEqualTo(13);
        assertThat(resolved.getAttacks().getFirst().getAttackBonus()).isEqualTo(4);
        assertThat(resolved.getFullStatBlock()).contains("N Small beast");
    }

    private CreatureTemplate.CreatureTemplateBuilder baseTemplate() {
        return CreatureTemplate.builder()
                .id("base")
                .name("Base")
                .summonLevel(1)
                .challengeRating("1/2")
                .alignment(Alignment.N)
                .size(CreatureSize.SMALL)
                .creatureType("beast")
                .subtypes(List.of())
                .allowedTemplates(List.of(SummonTemplateType.CHTHONIC, SummonTemplateType.FIERY, SummonTemplateType.CELESTIAL, SummonTemplateType.ENTROPIC, SummonTemplateType.RESOLUTE))
                .initiative(1)
                .senses(List.of("low-light vision"))
                .perception(4)
                .abilities(AbilityScores.builder()
                        .strength(10)
                        .dexterity(10)
                        .constitution(10)
                        .intelligence(2)
                        .wisdom(10)
                        .charisma(10)
                        .build())
                .armorClass(ArmorClass.builder()
                        .normal(12)
                        .touch(11)
                        .flatFooted(12)
                        .detail("+1 natural")
                        .build())
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(5)
                        .formula("1d8+1")
                        .hitDice(HitDice.builder().count(1).dieSize(8).build())
                        .build())
                .savingThrows(SavingThrows.builder()
                        .fortitude(2)
                        .reflex(2)
                        .will(0)
                        .fortitudeAbility(SavingThrowAbility.CONSTITUTION)
                        .build())
                .speeds(List.of(speed(SpeedType.LAND, 30)))
                .attacks(List.of(attack("Bite", 3, AttackAbility.STRENGTH, "1d4+1", DamageType.PIERCING)))
                .space("5 ft.")
                .reach("5 ft.")
                .specialAttacks(List.of())
                .specialDefenses(List.of())
                .tacticalNotes(List.of())
                .shortAbilities(List.of())
                .expandedAbilities(List.of())
                .fullStatBlock("base");
    }

    private Speed speed(SpeedType type, int valueFeet) {
        return Speed.builder()
                .type(type)
                .valueFeet(valueFeet)
                .build();
    }

    private Attack attack(String name, int attackBonus, AttackAbility attackAbility, String damageFormula, DamageType damageType) {
        return Attack.builder()
                .id(name.toLowerCase())
                .name(name)
                .attackBonus(attackBonus)
                .attackAbility(attackAbility)
                .quantity(1)
                .attackType(AttackType.MELEE)
                .damageComponents(List.of(DamageComponent.builder()
                        .formula(damageFormula)
                        .damageType(damageType)
                        .multipliesOnCritical(true)
                        .damageAbility(attackAbility == AttackAbility.NONE ? DamageAbility.NONE : DamageAbility.STRENGTH)
                        .damageAbilityMultiplier(1.0)
                        .build()))
                .critical(CriticalProfile.builder()
                        .threatRangeStart(20)
                        .multiplier(2)
                        .build())
                .notes(List.of())
                .build();
    }
}
