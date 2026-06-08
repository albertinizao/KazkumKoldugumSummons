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
        assertThat(resolved.getAttacks().getFirst().getDamageComponents().getLast().getDamageType()).isEqualTo(DamageType.ACID);
        assertThat(resolved.getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains("RESISTANCE:acid 10", "OTHER:listed immunities");
        assertThat(resolved.getFullStatBlock()).contains("NG Small beast");
    }

    @Test
    void appliesFieryTemplateEffectsWithoutTouchArmorBonus() {
        CreatureTemplate template = baseTemplate()
                .id("hound")
                .name("Hound")
                .allowedTemplates(List.of(SummonTemplateType.FIERY))
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
        assertThat(resolved.getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains("RESISTANCE:fire 10", "IMMUNITY:fire", "VULNERABILITY:cold");
    }

    @Test
    void calculatesMaximumHitPointsFromMaxDieRollPlusAugmentedConstitution() {
        CreatureTemplate template = baseTemplate()
                .id("antelope")
                .name("Antelope")
                .abilities(AbilityScores.builder()
                        .strength(10)
                        .dexterity(17)
                        .constitution(14)
                        .intelligence(2)
                        .wisdom(13)
                        .charisma(7)
                        .build())
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(6)
                        .formula("1d8+2")
                        .hitDice(HitDice.builder().count(1).dieSize(8).build())
                        .build())
                .build();

        ResolvedCreature resolved = resolver.resolve(template, null, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getMaxHitPoints()).isEqualTo(12);
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
                .allowedTemplates(List.of(SummonTemplateType.CELESTIAL))
                .speeds(List.of(speed(SpeedType.FLY, 60)))
                .attacks(List.of(attack("Aura", 0, AttackAbility.NONE, "—", DamageType.OTHER)))
                .build();

        ResolvedCreature resolved = resolver.resolve(template, SummonTemplateType.CELESTIAL, SummonerConfiguration.defaultConfiguration());

        assertThat(resolved.getAlignment()).isEqualTo(Alignment.NG);
        assertThat(resolved.getSpecialAttacks()).contains("Smite evil 1/day (swift action)");
        assertThat(resolved.getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains("RESISTANCE:cold 10", "RESISTANCE:acid 10", "RESISTANCE:electricity 10");
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
