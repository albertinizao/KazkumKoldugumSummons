package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.ActiveSummonGroup;
import com.pathfinder.summons.domain.model.ActiveSummonInstance;
import com.pathfinder.summons.domain.model.Alignment;
import com.pathfinder.summons.domain.model.ArmorClass;
import com.pathfinder.summons.domain.model.Attack;
import com.pathfinder.summons.domain.model.AttackAbility;
import com.pathfinder.summons.domain.model.AttackType;
import com.pathfinder.summons.domain.model.CreatureSize;
import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.CriticalProfile;
import com.pathfinder.summons.domain.model.DamageAbility;
import com.pathfinder.summons.domain.model.DamageComponent;
import com.pathfinder.summons.domain.model.DamageType;
import com.pathfinder.summons.domain.model.DiceRoll;
import com.pathfinder.summons.domain.model.HitDice;
import com.pathfinder.summons.domain.model.HitPointsDefinition;
import com.pathfinder.summons.domain.model.ResolvedCreature;
import com.pathfinder.summons.domain.model.SavingThrowAbility;
import com.pathfinder.summons.domain.model.SavingThrows;
import com.pathfinder.summons.domain.model.SingleAttackRollResult;
import com.pathfinder.summons.domain.model.Speed;
import com.pathfinder.summons.domain.model.SpeedType;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import com.pathfinder.summons.domain.service.DiceRoller;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CombatRollServiceTest {

    @Test
    void rollsAttacksSeparatelyAndHandlesCriticalThreats() {
        SequencedDiceRoller diceRoller = new SequencedDiceRoller()
                .enqueueD20(diceRoll("1d20+4", 19, 4, 23))
                .enqueueD20(diceRoll("1d20+4", 16, 4, 20))
                .enqueueD20(diceRoll("1d20+4", 12, 4, 16))
                .enqueueFormula(diceRoll("1d3+2", 2, 2, 4))
                .enqueueFormula(diceRoll("1", 0, 1, 1))
                .enqueueFormula(diceRoll("1d3+2", 2, 2, 4))
                .enqueueFormula(diceRoll("1", 0, 1, 1))
                .enqueueFormula(diceRoll("1d3+2", 3, 2, 5))
                .enqueueFormula(diceRoll("1", 0, 1, 1));

        CombatRollService service = new CombatRollService(diceRoller);
        ActiveSummonGroup group = groupWithOneInstance(oneAttackCreature());

        var result = service.rollGroupAttacks(group);

        assertThat(result.type()).isEqualTo(com.pathfinder.summons.domain.model.RollDisplayType.ATTACK_GROUP);
        assertThat(result.instanceResults()).hasSize(1);

        var creatureResult = result.instanceResults().getFirst();
        assertThat(creatureResult.attackResults()).hasSize(2);

        SingleAttackRollResult firstAttack = creatureResult.attackResults().getFirst();
        assertThat(firstAttack.attackIndex()).isEqualTo(1);
        assertThat(firstAttack.criticalThreat()).isNotNull();
        assertThat(firstAttack.normalDamage().components()).hasSize(2);
        assertThat(firstAttack.normalDamage().total()).isEqualTo(5);
        assertThat(firstAttack.criticalThreat().criticalDamage().total()).isEqualTo(9);

        SingleAttackRollResult secondAttack = creatureResult.attackResults().get(1);
        assertThat(secondAttack.attackIndex()).isEqualTo(2);
        assertThat(secondAttack.criticalThreat()).isNull();
        assertThat(secondAttack.normalDamage().total()).isEqualTo(6);
        assertThat(result.displayText()).contains("Daño si impacta");
    }

    @Test
    void rollsElementalDamageDiceForNaturalAttacks() {
        SequencedDiceRoller diceRoller = new SequencedDiceRoller()
                .enqueueD20(diceRoll("1d20+5", 11, 5, 16))
                .enqueueFormula(diceRoll("1d6+2", 4, 2, 6))
                .enqueueFormula(diceRoll("2d6", 7, 0, 7));

        CombatRollService service = new CombatRollService(diceRoller);
        ActiveSummonGroup group = groupWithOneInstance(creatureWithElementalDamage());

        var result = service.rollGroupAttacks(group);

        var attackResult = result.instanceResults().getFirst().attackResults().getFirst();
        assertThat(attackResult.normalDamage().components()).hasSize(2);
        assertThat(attackResult.normalDamage().components().get(0).roll().getFormula()).isEqualTo("1d6+2");
        assertThat(attackResult.normalDamage().components().get(1).roll().getFormula()).isEqualTo("2d6");
        assertThat(attackResult.normalDamage().total()).isEqualTo(13);
        assertThat(result.displayText()).contains("1d6+2 = 6 piercing + 2d6 = 7 fire");
    }

    @Test
    void rollsSavingThrowsForEveryInstance() {
        SequencedDiceRoller diceRoller = new SequencedDiceRoller()
                .enqueueD20(diceRoll("1d20+4", 12, 4, 16))
                .enqueueD20(diceRoll("1d20+2", 7, 2, 9))
                .enqueueD20(diceRoll("1d20+1", 15, 1, 16))
                .enqueueD20(diceRoll("1d20+4", 10, 4, 14))
                .enqueueD20(diceRoll("1d20+2", 4, 2, 6))
                .enqueueD20(diceRoll("1d20+1", 18, 1, 19));

        CombatRollService service = new CombatRollService(diceRoller);
        ActiveSummonGroup group = groupWithTwoInstances(oneAttackCreature());

        var result = service.rollGroupSavingThrows(group);

        assertThat(result.type()).isEqualTo(com.pathfinder.summons.domain.model.RollDisplayType.SAVING_THROWS_GROUP);
        assertThat(result.instanceResults()).hasSize(2);
        assertThat(result.instanceResults().getFirst().fortitude().getTotal()).isEqualTo(16);
        assertThat(result.instanceResults().getFirst().reflex().getTotal()).isEqualTo(9);
        assertThat(result.instanceResults().getFirst().will().getTotal()).isEqualTo(16);
        assertThat(result.displayText()).contains("Fortaleza", "Reflejos", "Voluntad");
    }

    private static ActiveSummonGroup groupWithOneInstance(ResolvedCreature creature) {
        return ActiveSummonGroup.builder()
                .id("group-1")
                .resolvedCreature(creature)
                .instances(List.of(ActiveSummonInstance.create("instance-1", 1, "Fiery Badger 1", creature.getMaxHitPoints())))
                .build();
    }

    private static ActiveSummonGroup groupWithTwoInstances(ResolvedCreature creature) {
        return ActiveSummonGroup.builder()
                .id("group-1")
                .resolvedCreature(creature)
                .instances(List.of(
                        ActiveSummonInstance.create("instance-1", 1, "Fiery Badger 1", creature.getMaxHitPoints()),
                        ActiveSummonInstance.create("instance-2", 2, "Fiery Badger 2", creature.getMaxHitPoints())))
                .build();
    }

    private static ResolvedCreature oneAttackCreature() {
        return ResolvedCreature.builder()
                .id("resolved-1")
                .baseTemplateId("badger")
                .displayName("Fiery Badger")
                .summonLevel(1)
                .appliedTemplate(null)
                .alignment(Alignment.NG)
                .size(CreatureSize.SMALL)
                .creatureType("animal")
                .subtypes(List.of())
                .initiative(1)
                .senses(List.of("low-light vision"))
                .perception(5)
                .armorClass(ArmorClass.builder().normal(13).touch(12).flatFooted(12).build())
                .maxHitPoints(6)
                .savingThrows(SavingThrows.builder()
                        .fortitude(4)
                        .reflex(2)
                        .will(1)
                        .fortitudeAbility(SavingThrowAbility.CONSTITUTION)
                        .build())
                .speeds(List.of(Speed.builder().type(SpeedType.LAND).valueFeet(30).build()))
                .speedsText("Speed 30 ft.")
                .attacks(List.of(Attack.builder()
                        .id("claw")
                        .name("Claw")
                        .attackBonus(4)
                        .attackAbility(AttackAbility.STRENGTH)
                        .quantity(2)
                        .attackType(AttackType.MELEE)
                        .damageComponents(List.of(
                                DamageComponent.builder()
                                        .formula("1d3+2")
                                        .damageType(DamageType.PIERCING)
                                        .multipliesOnCritical(true)
                                        .damageAbility(DamageAbility.STRENGTH)
                                        .damageAbilityMultiplier(1.0)
                                        .build(),
                                DamageComponent.builder()
                                        .formula("1")
                                        .damageType(DamageType.FIRE)
                                        .multipliesOnCritical(false)
                                        .damageAbility(DamageAbility.NONE)
                                        .damageAbilityMultiplier(0.0)
                                        .build()))
                        .critical(CriticalProfile.builder()
                                .threatRangeStart(19)
                                .multiplier(2)
                                .build())
                        .notes(List.of())
                        .build()))
                .attacksText("Claw +4 (1d3+2 + 1 fire)")
                .space("5 ft.")
                .reach("5 ft.")
                .specialAttacks(List.of())
                .specialDefenses(List.of())
                .shortAbilities(List.of())
                .expandedAbilities(List.of())
                .fullStatBlock("fiery badger")
                .appliedRules(List.of())
                .build();
    }

    private static ResolvedCreature creatureWithElementalDamage() {
        return ResolvedCreature.builder()
                .id("resolved-2")
                .baseTemplateId("badger")
                .displayName("Fiery Badger")
                .summonLevel(1)
                .appliedTemplate(SummonTemplateType.FIERY)
                .alignment(Alignment.NG)
                .size(CreatureSize.SMALL)
                .creatureType("animal")
                .subtypes(List.of("fire"))
                .initiative(1)
                .senses(List.of("low-light vision"))
                .perception(5)
                .armorClass(ArmorClass.builder().normal(13).touch(12).flatFooted(12).build())
                .maxHitPoints(6)
                .savingThrows(SavingThrows.builder()
                        .fortitude(4)
                        .reflex(2)
                        .will(1)
                        .fortitudeAbility(SavingThrowAbility.CONSTITUTION)
                        .build())
                .speeds(List.of(Speed.builder().type(SpeedType.LAND).valueFeet(30).build()))
                .speedsText("Speed 30 ft.")
                .attacks(List.of(Attack.builder()
                        .id("bite")
                        .name("Bite")
                        .attackBonus(5)
                        .attackAbility(AttackAbility.STRENGTH)
                        .quantity(1)
                        .attackType(AttackType.MELEE)
                        .damageComponents(List.of(
                                DamageComponent.builder()
                                        .formula("1d6+2")
                                        .damageType(DamageType.PIERCING)
                                        .multipliesOnCritical(true)
                                        .damageAbility(DamageAbility.STRENGTH)
                                        .damageAbilityMultiplier(1.0)
                                        .build(),
                                DamageComponent.builder()
                                        .formula("2d6")
                                        .damageType(DamageType.FIRE)
                                        .multipliesOnCritical(true)
                                        .damageAbility(DamageAbility.NONE)
                                        .damageAbilityMultiplier(0.0)
                                        .build()))
                        .critical(CriticalProfile.builder()
                                .threatRangeStart(20)
                                .multiplier(2)
                                .build())
                        .notes(List.of())
                        .build()))
                .attacksText("Bite +5 (1d6+2 + 2d6 fire)")
                .space("5 ft.")
                .reach("5 ft.")
                .specialAttacks(List.of())
                .specialDefenses(List.of())
                .shortAbilities(List.of())
                .expandedAbilities(List.of())
                .fullStatBlock("fiery badger")
                .appliedRules(List.of())
                .build();
    }

    private static DiceRoll diceRoll(String formula, int natural, int modifier, int total) {
        return DiceRoll.builder()
                .formula(formula)
                .naturalResults(natural == 0 ? List.of() : List.of(natural))
                .modifier(modifier)
                .total(total)
                .build();
    }

    private static final class SequencedDiceRoller implements DiceRoller {
        private final Queue<DiceRoll> d20Rolls = new ArrayDeque<>();
        private final Queue<DiceRoll> formulaRolls = new ArrayDeque<>();

        SequencedDiceRoller enqueueD20(DiceRoll roll) {
            d20Rolls.add(roll);
            return this;
        }

        SequencedDiceRoller enqueueFormula(DiceRoll roll) {
            formulaRolls.add(roll);
            return this;
        }

        @Override
        public DiceRoll roll(String formula) {
            DiceRoll roll = formulaRolls.poll();
            if (roll == null) {
                throw new IllegalStateException("Unexpected formula roll: " + formula);
            }
            return roll;
        }

        @Override
        public DiceRoll roll(int diceCount, int dieSize, int modifier) {
            if (dieSize == 20) {
                DiceRoll roll = d20Rolls.poll();
                if (roll == null) {
                    throw new IllegalStateException("Unexpected d20 roll with modifier " + modifier);
                }
                return roll;
            }
            DiceRoll roll = formulaRolls.poll();
            if (roll == null) {
                throw new IllegalStateException("Unexpected numeric roll with die size " + dieSize);
            }
            return roll;
        }
    }
}
