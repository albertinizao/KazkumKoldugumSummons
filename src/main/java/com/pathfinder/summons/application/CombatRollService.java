package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.ActiveSummonGroup;
import com.pathfinder.summons.domain.model.ActiveSummonInstance;
import com.pathfinder.summons.domain.model.Attack;
import com.pathfinder.summons.domain.model.CriticalProfile;
import com.pathfinder.summons.domain.model.CriticalThreatResult;
import com.pathfinder.summons.domain.model.CreatureAttackRollResult;
import com.pathfinder.summons.domain.model.CreatureSavingThrowsRollResult;
import com.pathfinder.summons.domain.model.DamageComponent;
import com.pathfinder.summons.domain.model.DamageComponentRollResult;
import com.pathfinder.summons.domain.model.DamageRollResult;
import com.pathfinder.summons.domain.model.DiceRoll;
import com.pathfinder.summons.domain.model.GroupAttackRollResult;
import com.pathfinder.summons.domain.model.GroupSavingThrowsRollResult;
import com.pathfinder.summons.domain.model.RollDisplayType;
import com.pathfinder.summons.domain.model.SavingThrows;
import com.pathfinder.summons.domain.model.SingleAttackRollResult;
import com.pathfinder.summons.domain.service.DiceRoller;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CombatRollService {

    private final DiceRoller diceRoller;

    public CombatRollService(DiceRoller diceRoller) {
        this.diceRoller = diceRoller;
    }

    public GroupAttackRollResult rollGroupAttacks(ActiveSummonGroup group) {
        List<CreatureAttackRollResult> instanceResults = group.getInstances().stream()
                .map(instance -> rollCreatureAttacks(instance, group))
                .toList();

        String creatureName = group.getResolvedCreature().getDisplayName();
        String title = "Atacar con todas: " + creatureName;
        return new GroupAttackRollResult(
                UUID.randomUUID().toString(),
                RollDisplayType.ATTACK_GROUP,
                title,
                group.getId(),
                creatureName,
                LocalDateTime.now(),
                instanceResults,
                formatGroupAttackDisplayText(instanceResults));
    }

    public GroupSavingThrowsRollResult rollGroupSavingThrows(ActiveSummonGroup group) {
        List<CreatureSavingThrowsRollResult> instanceResults = group.getInstances().stream()
                .map(instance -> rollCreatureSavingThrows(instance, group.getResolvedCreature().getSavingThrows()))
                .toList();

        String creatureName = group.getResolvedCreature().getDisplayName();
        String title = "Tirar TS: " + creatureName;
        return new GroupSavingThrowsRollResult(
                UUID.randomUUID().toString(),
                RollDisplayType.SAVING_THROWS_GROUP,
                title,
                group.getId(),
                creatureName,
                LocalDateTime.now(),
                instanceResults,
                formatGroupSavingThrowsDisplayText(instanceResults));
    }

    public String formatAttackRollSummary(GroupAttackRollResult result) {
        return result.displayText();
    }

    public String formatSavingThrowsSummary(GroupSavingThrowsRollResult result) {
        return result.displayText();
    }

    private CreatureAttackRollResult rollCreatureAttacks(ActiveSummonInstance instance, ActiveSummonGroup group) {
        List<SingleAttackRollResult> attackResults = new ArrayList<>();
        for (Attack attack : group.getResolvedCreature().getAttacks()) {
            for (int index = 1; index <= Math.max(1, attack.getQuantity()); index++) {
                attackResults.add(rollSingleAttack(attack, attack.getQuantity() > 1 ? index : null));
            }
        }

        return new CreatureAttackRollResult(
                instance.getId(),
                instance.getDisplayName(),
                List.copyOf(attackResults),
                formatCreatureAttackDisplayText(instance.getDisplayName(), attackResults));
    }

    private CreatureSavingThrowsRollResult rollCreatureSavingThrows(ActiveSummonInstance instance, SavingThrows savingThrows) {
        DiceRoll fortitude = diceRoller.roll(1, 20, savingThrows.getFortitude());
        DiceRoll reflex = diceRoller.roll(1, 20, savingThrows.getReflex());
        DiceRoll will = diceRoller.roll(1, 20, savingThrows.getWill());

        return new CreatureSavingThrowsRollResult(
                instance.getId(),
                instance.getDisplayName(),
                fortitude,
                reflex,
                will,
                formatCreatureSavingThrowsDisplayText(instance.getDisplayName(), fortitude, reflex, will));
    }

    private SingleAttackRollResult rollSingleAttack(Attack attack, Integer attackIndex) {
        DiceRoll attackRoll = diceRoller.roll(1, 20, attack.getAttackBonus());
        DamageRollResult normalDamage = rollDamage(attack, false);
        CriticalThreatResult criticalThreat = isCriticalThreat(attack, attackRoll)
                ? new CriticalThreatResult(
                        diceRoller.roll(1, 20, attack.getAttackBonus()),
                        rollDamage(attack, true))
                : null;

        return new SingleAttackRollResult(
                attack.getId(),
                attack.getName(),
                attackIndex,
                attackRoll,
                normalDamage,
                criticalThreat,
                formatSingleAttackDisplayText(attack, attackIndex, attackRoll, normalDamage, criticalThreat));
    }

    private boolean isCriticalThreat(Attack attack, DiceRoll attackRoll) {
        CriticalProfile critical = attack.getCritical();
        if (critical == null || attackRoll.getNaturalResults().isEmpty()) {
            return false;
        }

        int naturalRoll = attackRoll.getNaturalResults().getFirst();
        return naturalRoll >= critical.getThreatRangeStart();
    }

    private DamageRollResult rollDamage(Attack attack, boolean critical) {
        int multiplier = critical && attack.getCritical() != null ? Math.max(1, attack.getCritical().getMultiplier()) : 1;
        List<DamageComponentRollResult> components = attack.getDamageComponents().stream()
                .map(component -> rollDamageComponent(component, multiplier))
                .toList();

        int total = components.stream().mapToInt(DamageComponentRollResult::total).sum();
        return new DamageRollResult(
                List.copyOf(components),
                total,
                formatDamageDisplayText(components));
    }

    private DamageComponentRollResult rollDamageComponent(DamageComponent component, int multiplier) {
        DiceRoll roll = diceRoller.roll(component.getFormula());
        int componentMultiplier = component.isMultipliesOnCritical() ? multiplier : 1;
        int total = roll.getTotal() * componentMultiplier;
        return new DamageComponentRollResult(
                component.getFormula(),
                roll,
                component.getDamageType(),
                component.isMultipliesOnCritical(),
                total);
    }

    private String formatGroupAttackDisplayText(List<CreatureAttackRollResult> instanceResults) {
        return instanceResults.stream()
                .map(CreatureAttackRollResult::displayText)
                .collect(Collectors.joining("\n\n"));
    }

    private String formatGroupSavingThrowsDisplayText(List<CreatureSavingThrowsRollResult> instanceResults) {
        return instanceResults.stream()
                .map(CreatureSavingThrowsRollResult::displayText)
                .collect(Collectors.joining("\n\n"));
    }

    private String formatCreatureAttackDisplayText(String instanceDisplayName, List<SingleAttackRollResult> attackResults) {
        return instanceDisplayName + "\n" + attackResults.stream()
                .map(SingleAttackRollResult::displayText)
                .collect(Collectors.joining("\n"));
    }

    private String formatCreatureSavingThrowsDisplayText(String instanceDisplayName, DiceRoll fortitude, DiceRoll reflex, DiceRoll will) {
        return String.join("\n",
                instanceDisplayName,
                "Fortaleza: " + formatRollLine(fortitude, true),
                "Reflejos: " + formatRollLine(reflex, true),
                "Voluntad: " + formatRollLine(will, true));
    }

    private String formatSingleAttackDisplayText(Attack attack,
                                                 Integer attackIndex,
                                                 DiceRoll attackRoll,
                                                 DamageRollResult normalDamage,
                                                 CriticalThreatResult criticalThreat) {
        String attackLabel = attack.getName() + (attackIndex == null ? "" : " " + attackIndex);
        List<String> lines = new ArrayList<>();
        lines.add(attackLabel + ": " + formatRollLine(attackRoll, true));
        lines.add("Daño si impacta: " + normalDamage.displayText());
        if (criticalThreat != null) {
            lines.add("Amenaza de crítico: " + formatRollLine(attackRoll, true));
            lines.add("Confirmación: " + formatRollLine(criticalThreat.confirmationRoll(), true));
            lines.add("Daño crítico: " + criticalThreat.criticalDamage().displayText());
        }
        return String.join("\n", lines);
    }

    private String formatDamageDisplayText(List<DamageComponentRollResult> components) {
        return components.stream()
                .map(component -> component.formula() + " = " + component.total() + " " + damageLabel(component))
                .collect(Collectors.joining(" + "));
    }

    private String damageLabel(DamageComponentRollResult component) {
        return component.damageType().name().toLowerCase(Locale.ROOT);
    }

    private String formatRollLine(DiceRoll roll, boolean attackStyle) {
        String formula = roll.getFormula();
        if (attackStyle && formula != null && formula.startsWith("1d20")) {
            formula = "d20" + formula.substring("1d20".length());
        }

        return formula + " " + formatNaturalResults(roll.getNaturalResults()) + " + " + roll.getModifier() + " = " + roll.getTotal();
    }

    private String formatNaturalResults(List<Integer> naturalResults) {
        if (naturalResults == null || naturalResults.isEmpty()) {
            return "0";
        }
        if (naturalResults.size() == 1) {
            return String.valueOf(naturalResults.getFirst());
        }
        return naturalResults.stream().map(String::valueOf).collect(Collectors.joining(" + "));
    }
}
