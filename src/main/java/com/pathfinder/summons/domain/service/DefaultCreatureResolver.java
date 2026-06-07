package com.pathfinder.summons.domain.service;

import com.pathfinder.summons.domain.model.AbilityDetail;
import com.pathfinder.summons.domain.model.AbilityScores;
import com.pathfinder.summons.domain.model.AbilitySummary;
import com.pathfinder.summons.domain.model.Alignment;
import com.pathfinder.summons.domain.model.AppliedRule;
import com.pathfinder.summons.domain.model.ArmorClass;
import com.pathfinder.summons.domain.model.Attack;
import com.pathfinder.summons.domain.model.AttackAbility;
import com.pathfinder.summons.domain.model.AttackType;
import com.pathfinder.summons.domain.model.CreatureSize;
import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.DamageAbility;
import com.pathfinder.summons.domain.model.DamageComponent;
import com.pathfinder.summons.domain.model.DamageType;
import com.pathfinder.summons.domain.model.FixedRuleType;
import com.pathfinder.summons.domain.model.HitDice;
import com.pathfinder.summons.domain.model.HitPointsDefinition;
import com.pathfinder.summons.domain.model.ResolvedCreature;
import com.pathfinder.summons.domain.model.SavingThrows;
import com.pathfinder.summons.domain.model.Speed;
import com.pathfinder.summons.domain.model.SpeedType;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import com.pathfinder.summons.domain.model.SummonerConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DefaultCreatureResolver implements CreatureResolver {

    @Override
    public ResolvedCreature resolve(CreatureTemplate template, SummonTemplateType templateType, SummonerConfiguration config) {
        if (templateType != null && !template.getAllowedTemplates().contains(templateType)) {
            throw new IllegalArgumentException("La criatura " + template.getName() + " no permite la plantilla " + templateType);
        }

        AbilityScores baseAbilities = template.getAbilities();
        AbilityScores augmentedAbilities = augmentAbilities(baseAbilities);

        List<Speed> resolvedSpeeds = resolveSpeeds(template, templateType);
        boolean deepGuardianApplied = hasDeepGuardian(resolvedSpeeds, templateType);

        List<Attack> resolvedAttacks = resolveAttacks(template.getAttacks(), baseAbilities, augmentedAbilities, deepGuardianApplied, templateType);
        ArmorClass armorClass = resolveArmorClass(template.getArmorClass(), deepGuardianApplied);
        SavingThrows savingThrows = resolveSavingThrows(template.getSavingThrows(), baseAbilities, augmentedAbilities);
        int maxHitPoints = resolveHitPoints(template.getHitPoints(), baseAbilities, augmentedAbilities);
        String speedsText = formatSpeeds(resolvedSpeeds);
        String attacksText = formatAttacks(resolvedAttacks);
        String displayName = buildDisplayName(template.getName(), templateType);

        List<AppliedRule> appliedRules = new ArrayList<>();
        appliedRules.add(AppliedRule.builder()
                .type(FixedRuleType.AUGMENT_SUMMONING)
                .description("+4 Str, +4 Con")
                .build());

        if (templateType != null) {
            appliedRules.add(AppliedRule.builder()
                    .type(FixedRuleType.VERSATILE_SUMMON_MONSTER)
                    .description(templateLabel(templateType) + " template")
                    .build());
        }

        if (deepGuardianApplied) {
            appliedRules.add(AppliedRule.builder()
                    .type(FixedRuleType.DEEP_GUARDIAN)
                    .description("+1 attack, +1 AC")
                    .build());
        }

        return ResolvedCreature.builder()
                .id(buildResolvedId(template.getId(), templateType, deepGuardianApplied))
                .baseTemplateId(template.getId())
                .displayName(displayName)
                .summonLevel(template.getSummonLevel())
                .appliedTemplate(templateType)
                .alignment(resolveAlignment(template.getAlignment(), templateType))
                .size(template.getSize())
                .creatureType(template.getCreatureType())
                .subtypes(resolveSubtypes(template.getSubtypes(), templateType))
                .initiative(template.getInitiative())
                .senses(template.getSenses())
                .perception(template.getPerception())
                .armorClass(armorClass)
                .maxHitPoints(maxHitPoints)
                .savingThrows(savingThrows)
                .speeds(resolvedSpeeds)
                .speedsText(speedsText)
                .attacks(resolvedAttacks)
                .attacksText(attacksText)
                .space(template.getSpace())
                .reach(template.getReach())
                .specialAttacks(resolveSpecialAttacks(template.getSpecialAttacks(), templateType))
                .specialDefenses(resolveSpecialDefenses(template.getSpecialDefenses(), templateType))
                .shortAbilities(template.getShortAbilities())
                .expandedAbilities(template.getExpandedAbilities())
                .fullStatBlock(buildFullStatBlock(displayName, template, armorClass, maxHitPoints, savingThrows, resolvedSpeeds, resolvedAttacks))
                .appliedRules(appliedRules)
                .build();
    }

    private AbilityScores augmentAbilities(AbilityScores baseAbilities) {
        return AbilityScores.builder()
                .strength(baseAbilities.getStrength() + 4)
                .dexterity(baseAbilities.getDexterity())
                .constitution(baseAbilities.getConstitution() + 4)
                .intelligence(baseAbilities.getIntelligence())
                .wisdom(baseAbilities.getWisdom())
                .charisma(baseAbilities.getCharisma())
                .build();
    }

    private List<Speed> resolveSpeeds(CreatureTemplate template, SummonTemplateType templateType) {
        List<Speed> speeds = template.getSpeeds().stream()
                .map(speed -> Speed.builder()
                        .type(speed.getType())
                        .valueFeet(speed.getValueFeet())
                        .maneuverability(speed.getManeuverability())
                        .notes(speed.getNotes())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        if (templateType == SummonTemplateType.CHTHONIC && speeds.stream().noneMatch(speed -> speed.getType() == SpeedType.BURROW)) {
            int highestSpeed = speeds.stream().mapToInt(Speed::getValueFeet).max().orElse(0);
            speeds.add(Speed.builder()
                    .type(SpeedType.BURROW)
                    .valueFeet(Math.max(5, highestSpeed / 2))
                    .build());
        }

        return List.copyOf(speeds);
    }

    private boolean hasDeepGuardian(List<Speed> resolvedSpeeds, SummonTemplateType templateType) {
        boolean hasBurrow = resolvedSpeeds.stream().anyMatch(speed -> speed.getType() == SpeedType.BURROW);
        boolean hasEarthSubtype = templateType == SummonTemplateType.CHTHONIC;
        return hasBurrow || hasEarthSubtype;
    }

    private List<Attack> resolveAttacks(List<Attack> baseAttacks,
                                        AbilityScores baseAbilities,
                                        AbilityScores augmentedAbilities,
                                        boolean deepGuardianApplied,
                                        SummonTemplateType templateType) {
        int strengthDelta = augmentedAbilities.getStrengthModifier() - baseAbilities.getStrengthModifier();
        int dexterityDelta = augmentedAbilities.getDexterityModifier() - baseAbilities.getDexterityModifier();
        int attackBonusAdjustment = deepGuardianApplied ? 1 : 0;

        return baseAttacks.stream()
                .map(attack -> {
                    int abilityDelta = switch (attack.getAttackAbility()) {
                        case DEXTERITY -> dexterityDelta;
                        case NONE, OTHER -> 0;
                        case STRENGTH -> strengthDelta;
                    };

                    List<DamageComponent> adjustedComponents = attack.getDamageComponents().stream()
                            .map(component -> adjustDamageComponent(component, strengthDelta, dexterityDelta))
                            .toList();

                    List<DamageComponent> templateComponents = templateDamageComponents(templateType);
                    List<DamageComponent> mergedComponents = new ArrayList<>(adjustedComponents);
                    mergedComponents.addAll(templateComponents);

                    return Attack.builder()
                            .id(attack.getId())
                            .name(attack.getName())
                            .attackBonus(attack.getAttackBonus() + abilityDelta + attackBonusAdjustment)
                            .attackAbility(attack.getAttackAbility())
                            .quantity(attack.getQuantity())
                            .attackType(attack.getAttackType())
                            .damageComponents(List.copyOf(mergedComponents))
                            .critical(attack.getCritical())
                            .notes(attack.getNotes())
                            .build();
                })
                .toList();
    }

    private DamageComponent adjustDamageComponent(DamageComponent component,
                                                  int strengthDelta,
                                                  int dexterityDelta) {
        int abilityDelta = switch (component.getDamageAbility()) {
            case DEXTERITY -> dexterityDelta;
            case NONE, OTHER -> 0;
            case STRENGTH -> strengthDelta * (int) Math.round(component.getDamageAbilityMultiplier() <= 0 ? 1 : component.getDamageAbilityMultiplier());
        };

        return DamageComponent.builder()
                .formula(adjustFormula(component.getFormula(), abilityDelta))
                .damageType(component.getDamageType())
                .multipliesOnCritical(component.isMultipliesOnCritical())
                .damageAbility(component.getDamageAbility())
                .damageAbilityMultiplier(component.getDamageAbilityMultiplier())
                .label(component.getLabel())
                .build();
    }

    private List<DamageComponent> templateDamageComponents(SummonTemplateType templateType) {
        if (templateType == null) {
            return List.of();
        }

        if (templateType == SummonTemplateType.FIERY) {
            return List.of(DamageComponent.builder()
                    .formula("1")
                    .damageType(DamageType.FIRE)
                    .multipliesOnCritical(false)
                    .damageAbility(DamageAbility.NONE)
                    .damageAbilityMultiplier(0.0)
                    .label("fire")
                    .build());
        }

        if (templateType == SummonTemplateType.CHTHONIC) {
            return List.of(DamageComponent.builder()
                    .formula("1")
                    .damageType(DamageType.ACID)
                    .multipliesOnCritical(false)
                    .damageAbility(DamageAbility.NONE)
                    .damageAbilityMultiplier(0.0)
                    .label("acid")
                    .build());
        }

        return List.of();
    }

    private ArmorClass resolveArmorClass(ArmorClass baseArmorClass, boolean deepGuardianApplied) {
        int bonus = deepGuardianApplied ? 1 : 0;
        String detail = baseArmorClass.getDetail();
        String resolvedDetail = bonus == 0
                ? detail
                : (detail == null || detail.isBlank() ? "+1 Deep Guardian" : detail + ", +1 Deep Guardian");

        return ArmorClass.builder()
                .normal(baseArmorClass.getNormal() + bonus)
                .touch(baseArmorClass.getTouch())
                .flatFooted(baseArmorClass.getFlatFooted() + bonus)
                .detail(resolvedDetail)
                .build();
    }

    private SavingThrows resolveSavingThrows(SavingThrows baseSavingThrows, AbilityScores baseAbilities, AbilityScores augmentedAbilities) {
        int fortitudeDelta = augmentedAbilities.getConstitutionModifier() - baseAbilities.getConstitutionModifier();
        return SavingThrows.builder()
                .fortitude(baseSavingThrows.getFortitude() + fortitudeDelta)
                .reflex(baseSavingThrows.getReflex())
                .will(baseSavingThrows.getWill())
                .fortitudeAbility(baseSavingThrows.getFortitudeAbility())
                .build();
    }

    private int resolveHitPoints(HitPointsDefinition hitPoints, AbilityScores baseAbilities, AbilityScores augmentedAbilities) {
        int hitDiceCount = Optional.ofNullable(hitPoints.getHitDice())
                .map(HitDice::getCount)
                .orElseGet(() -> inferHitDiceCount(hitPoints.getFormula()));
        int conDelta = augmentedAbilities.getConstitutionModifier() - baseAbilities.getConstitutionModifier();
        return hitPoints.getMaximum() + (hitDiceCount * conDelta);
    }

    private int inferHitDiceCount(String formula) {
        if (formula == null || formula.isBlank()) {
            return 1;
        }

        int dIndex = formula.toLowerCase(Locale.ROOT).indexOf('d');
        if (dIndex <= 0) {
            return 1;
        }

        try {
            return Integer.parseInt(formula.substring(0, dIndex).trim());
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private String formatSpeeds(List<Speed> speeds) {
        if (speeds.isEmpty()) {
            return "—";
        }

        return speeds.stream()
                .map(speed -> {
                    String label = speed.getType() == SpeedType.LAND ? "Speed" : speed.getType().name().toLowerCase(Locale.ROOT);
                    return label + " " + speed.getValueFeet() + " ft.";
                })
                .collect(Collectors.joining(", "));
    }

    private String formatAttacks(List<Attack> attacks) {
        if (attacks.isEmpty()) {
            return "—";
        }

        return attacks.stream()
                .map(this::formatAttack)
                .collect(Collectors.joining(", "));
    }

    private String formatAttack(Attack attack) {
        String attackType = switch (attack.getAttackType()) {
            case MELEE -> "Melee";
            case RANGED -> "Ranged";
            case TOUCH -> "Touch";
            case SPECIAL -> "Special";
        };

        String attackLabel = attack.getQuantity() > 1
                ? attack.getQuantity() + " " + pluralize(attack.getName().toLowerCase(Locale.ROOT))
                : attack.getName().toLowerCase(Locale.ROOT);
        String damage = attack.getDamageComponents().stream()
                .map(this::formatDamageComponent)
                .collect(Collectors.joining(" + "));
        return attackType + " " + attackLabel + " +" + attack.getAttackBonus() + " (" + damage + ")";
    }

    private String formatDamageComponent(DamageComponent component) {
        String label = component.getLabel();
        if (label != null && !label.isBlank()) {
            return component.getFormula() + " " + label;
        }

        if (component.getDamageType() == DamageType.PIERCING
                || component.getDamageType() == DamageType.SLASHING
                || component.getDamageType() == DamageType.BLUDGEONING) {
            return component.getFormula();
        }

        return component.getFormula() + " " + component.getDamageType().name().toLowerCase(Locale.ROOT);
    }

    private String pluralize(String name) {
        return name.endsWith("s") ? name : name + "s";
    }

    private String adjustFormula(String formula, int delta) {
        if (delta == 0 || formula == null || formula.isBlank()) {
            return formula;
        }

        String normalized = formula.replace(" ", "");
        int plusIndex = normalized.lastIndexOf('+');
        int minusIndex = normalized.lastIndexOf('-');
        int signIndex = Math.max(plusIndex, minusIndex > 0 ? minusIndex : -1);

        if (signIndex > 0) {
            String prefix = normalized.substring(0, signIndex);
            try {
                int current = Integer.parseInt(normalized.substring(signIndex));
                int adjusted = current + delta;
                return prefix + formatSigned(adjusted);
            } catch (NumberFormatException ex) {
                return normalized + formatSigned(delta);
            }
        }

        return normalized + formatSigned(delta);
    }

    private String formatSigned(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }

    private String buildDisplayName(String baseName, SummonTemplateType templateType) {
        if (templateType == null) {
            return baseName;
        }

        return templateLabel(templateType) + " " + baseName;
    }

    private String templateLabel(SummonTemplateType templateType) {
        return switch (templateType) {
            case CHTHONIC -> "Chthonic";
            case FIERY -> "Fiery";
            case CELESTIAL -> "Celestial";
            case ENTROPIC -> "Entropic";
            case RESOLUTE -> "Resolute";
        };
    }

    private String buildResolvedId(String baseId, SummonTemplateType templateType, boolean deepGuardianApplied) {
        StringBuilder builder = new StringBuilder(baseId);
        if (templateType != null) {
            builder.append(':').append(templateType.name());
        }
        builder.append(":AUGMENT_SUMMONING");
        if (deepGuardianApplied) {
            builder.append(":DEEP_GUARDIAN");
        }
        return builder.toString();
    }

    private Alignment resolveAlignment(Alignment baseAlignment, SummonTemplateType templateType) {
        return baseAlignment;
    }

    private List<String> resolveSubtypes(List<String> baseSubtypes, SummonTemplateType templateType) {
        List<String> subtypes = new ArrayList<>(baseSubtypes);
        if (templateType == SummonTemplateType.CHTHONIC && subtypes.stream().map(value -> value.toLowerCase(Locale.ROOT)).noneMatch(subtype -> subtype.contains("earth"))) {
            subtypes.add("earth");
        }
        return List.copyOf(subtypes);
    }

    private List<String> resolveSpecialAttacks(List<String> specialAttacks, SummonTemplateType templateType) {
        return specialAttacks;
    }

    private List<com.pathfinder.summons.domain.model.SpecialDefense> resolveSpecialDefenses(List<com.pathfinder.summons.domain.model.SpecialDefense> specialDefenses,
                                                                                            SummonTemplateType templateType) {
        return specialDefenses;
    }

    private String buildFullStatBlock(String displayName,
                                      CreatureTemplate template,
                                      ArmorClass armorClass,
                                      int maxHitPoints,
                                      SavingThrows savingThrows,
                                      List<Speed> speeds,
                                      List<Attack> attacks) {
        String senses = String.join(", ", template.getSenses());
        String speedText = formatSpeeds(speeds);
        String attackText = formatAttacks(attacks);
        return displayName + "\n"
                + template.getAlignment() + " " + template.getSize().name().charAt(0) + template.getSize().name().substring(1).toLowerCase(Locale.ROOT) + " " + template.getCreatureType() + "\n"
                + "Init +" + template.getInitiative() + "; Senses " + senses + "; Perception +" + template.getPerception() + "\n"
                + "AC " + armorClass.getNormal() + ", touch " + armorClass.getTouch() + ", flat-footed " + armorClass.getFlatFooted() + " (" + armorClass.getDetail() + ")\n"
                + "hp " + maxHitPoints + " (" + template.getHitPoints().getFormula() + ")\n"
                + "Fort +" + savingThrows.getFortitude() + ", Ref +" + savingThrows.getReflex() + ", Will +" + savingThrows.getWill() + "\n"
                + speedText + "\n"
                + attackText + "\n"
                + "Space " + template.getSpace() + "; Reach " + template.getReach() + "\n"
                + "Special Attacks " + String.join(", ", template.getSpecialAttacks());
    }
}
