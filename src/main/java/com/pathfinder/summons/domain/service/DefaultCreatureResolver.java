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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DefaultCreatureResolver implements CreatureResolver {

    private static final Set<String> NATURAL_ATTACK_NAMES = Set.of(
            "bite",
            "claw",
            "gore",
            "hoof",
            "tentacle",
            "wing",
            "pincer",
            "tail",
            "slam",
            "sting",
            "talon",
            "other");

    @Override
    public ResolvedCreature resolve(CreatureTemplate template, SummonTemplateType templateType, SummonerConfiguration config) {
        if (templateType != null && isOutsider(template)) {
            throw new IllegalArgumentException("La criatura " + template.getName() + " no permite plantillas porque es un outsider");
        }

        AbilityScores baseAbilities = template.getAbilities();
        AbilityScores augmentedAbilities = augmentAbilities(baseAbilities);

        List<Speed> resolvedSpeeds = resolveSpeeds(template, templateType);
        List<String> resolvedSenses = resolveSenses(template.getSenses(), templateType, resolvedSpeeds);
        int hitDiceCount = resolveHitDiceCount(template.getHitPoints());
        List<String> resolvedSpecialAttacks = resolveSpecialAttacks(template.getSpecialAttacks(), templateType, hitDiceCount, baseAbilities.getCharismaModifier());
        List<com.pathfinder.summons.domain.model.SpecialDefense> resolvedSpecialDefenses = resolveSpecialDefenses(template, templateType, hitDiceCount, template.getChallengeRating());
        boolean deepGuardianApplied = hasDeepGuardian(template, resolvedSpeeds);

        List<Attack> resolvedAttacks = resolveAttacks(template.getAttacks(), baseAbilities, augmentedAbilities, deepGuardianApplied, templateType, hitDiceCount);
        ArmorClass armorClass = resolveArmorClass(template.getArmorClass(), deepGuardianApplied);
        SavingThrows savingThrows = resolveSavingThrows(template.getSavingThrows(), baseAbilities, augmentedAbilities);
        int maxHitPoints = resolveHitPoints(template.getHitPoints(), baseAbilities, augmentedAbilities);
        String hitPointsFormula = template.getHitPoints().getFormula();
        int cmb = resolveCombatManeuverBonus(resolvedAttacks);
        int cmd = 10 + cmb + baseAbilities.getDexterityModifier();
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
                .challengeRating(template.getChallengeRating())
                .appliedTemplate(templateType)
                .alignment(resolveAlignment(template.getAlignment(), templateType))
                .size(template.getSize())
                .creatureType(template.getCreatureType())
                .subtypes(resolveSubtypes(template.getSubtypes(), templateType))
                .initiative(template.getInitiative())
                .senses(resolvedSenses)
                .perception(template.getPerception())
                .armorClass(armorClass)
                .maxHitPoints(maxHitPoints)
                .hitPointsFormula(hitPointsFormula)
                .cmb(cmb)
                .cmd(cmd)
                .savingThrows(savingThrows)
                .speeds(resolvedSpeeds)
                .speedsText(speedsText)
                .attacks(resolvedAttacks)
                .attacksText(attacksText)
                .space(template.getSpace())
                .reach(template.getReach())
                .specialAttacks(resolvedSpecialAttacks)
                .specialDefenses(resolvedSpecialDefenses)
                .shortAbilities(template.getShortAbilities())
                .expandedAbilities(template.getExpandedAbilities())
                .fullStatBlock(buildFullStatBlock(
                        displayName,
                        resolveAlignment(template.getAlignment(), templateType),
                        template.getSize(),
                        template.getCreatureType(),
                        resolveSubtypes(template.getSubtypes(), templateType),
                        template.getInitiative(),
                        resolvedSenses,
                        template.getPerception(),
                        armorClass,
                        maxHitPoints,
                        savingThrows,
                        resolvedSpeeds,
                        resolvedAttacks,
                        template.getSpace(),
                        template.getReach(),
                        resolvedSpecialAttacks,
                        resolvedSpecialDefenses))
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

    private List<String> resolveSenses(List<String> baseSenses,
                                       SummonTemplateType templateType,
                                       List<Speed> resolvedSpeeds) {
        List<String> senses = new ArrayList<>(baseSenses == null ? List.of() : baseSenses);
        if (templateType != null) {
            appendIfMissing(senses, "darkvision 60 ft.");
        }

        if (templateType == SummonTemplateType.CHTHONIC || hasBurrowSpeed(resolvedSpeeds)) {
            appendIfMissing(senses, "tremorsense 60 ft.");
        }

        return List.copyOf(senses);
    }

    private boolean hasDeepGuardian(CreatureTemplate template, List<Speed> resolvedSpeeds) {
        return hasBurrowSpeed(resolvedSpeeds) || hasEarthSubtype(template.getSubtypes());
    }

    private boolean isOutsider(CreatureTemplate template) {
        String creatureType = template.getCreatureType();
        return creatureType != null && creatureType.toLowerCase(Locale.ROOT).contains("outsider");
    }

    private boolean hasBurrowSpeed(List<Speed> speeds) {
        return speeds.stream().anyMatch(speed -> speed.getType() == SpeedType.BURROW);
    }

    private boolean hasEarthSubtype(List<String> subtypes) {
        return subtypes.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(subtype -> subtype.contains("earth"));
    }

    private List<Attack> resolveAttacks(List<Attack> baseAttacks,
                                        AbilityScores baseAbilities,
                                        AbilityScores augmentedAbilities,
                                        boolean deepGuardianApplied,
                                        SummonTemplateType templateType,
                                        int hitDiceCount) {
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
                            .map(component -> adjustDamageComponent(attack, component, strengthDelta, dexterityDelta))
                            .toList();

                    List<DamageComponent> templateComponents = templateDamageComponents(templateType, attack, hitDiceCount);
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

    private DamageComponent adjustDamageComponent(Attack attack,
                                                  DamageComponent component,
                                                  int strengthDelta,
                                                  int dexterityDelta) {
        int abilityDelta = switch (component.getDamageAbility()) {
            case DEXTERITY -> dexterityDelta;
            case NONE, OTHER -> 0;
            case STRENGTH -> strengthDelta * (int) Math.round(component.getDamageAbilityMultiplier() <= 0 ? 1 : component.getDamageAbilityMultiplier());
        };

        return DamageComponent.builder()
                .formula(adjustFormula(component.getFormula(), abilityDelta))
                .damageType(resolveDamageType(attack, component))
                .multipliesOnCritical(component.isMultipliesOnCritical())
                .damageAbility(component.getDamageAbility())
                .damageAbilityMultiplier(component.getDamageAbilityMultiplier())
                .label(component.getLabel())
                .build();
    }

    private List<DamageComponent> templateDamageComponents(SummonTemplateType templateType, Attack attack, int hitDiceCount) {
        if (templateType == null || !isNaturalAttack(attack)) {
            return List.of();
        }

        String formula = templateDamageFormula(templateType, hitDiceCount);
        if (formula == null) {
            return List.of();
        }

        DamageType damageType = templateType == SummonTemplateType.FIERY ? DamageType.FIRE : DamageType.ACID;
        if (damageType == null) {
            return List.of();
        }

        return List.of(DamageComponent.builder()
                .formula(formula)
                .damageType(damageType)
                .multipliesOnCritical(true)
                .damageAbility(DamageAbility.NONE)
                .damageAbilityMultiplier(0.0)
                .label(damageType.name().toLowerCase(Locale.ROOT))
                .build());
    }

    private String templateDamageFormula(SummonTemplateType templateType, int hitDiceCount) {
        if (templateType == SummonTemplateType.FIERY) {
            if (hitDiceCount >= 11) {
                return "2d6";
            }
            if (hitDiceCount >= 5) {
                return "1d6";
            }
            return "1";
        }

        if (templateType == SummonTemplateType.CHTHONIC) {
            if (hitDiceCount >= 11) {
                return "2d6";
            }
            if (hitDiceCount >= 5) {
                return "1d6";
            }
            return "1";
        }
        return null;
    }

    private boolean isNaturalAttack(Attack attack) {
        if (attack == null || attack.getAttackType() != AttackType.MELEE) {
            return false;
        }

        String name = attack.getName();
        if (name == null || name.isBlank()) {
            return false;
        }

        return NATURAL_ATTACK_NAMES.contains(normalizeAttackName(name)) || normalizeAttackName(name).startsWith("tail");
    }

    private String normalizeAttackName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private DamageType resolveDamageType(Attack attack, DamageComponent component) {
        if (component.getDamageType() != DamageType.OTHER || attack == null) {
            return component.getDamageType();
        }

        String attackName = normalizeAttackName(attack.getName());
        if (attackName.startsWith("bite") || attackName.startsWith("gore") || attackName.startsWith("sting")) {
            return DamageType.PIERCING;
        }
        if (attackName.startsWith("claw") || attackName.startsWith("talon") || attackName.startsWith("wing")) {
            return DamageType.SLASHING;
        }
        if (attackName.startsWith("hoof")
                || attackName.startsWith("tentacle")
                || attackName.startsWith("pincer")
                || attackName.startsWith("tail")
                || attackName.startsWith("slam")) {
            return DamageType.BLUDGEONING;
        }

        return DamageType.BLUDGEONING;
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
        int hitDiceCount = resolveHitDiceCount(hitPoints);
        int conDelta = augmentedAbilities.getConstitutionModifier() - baseAbilities.getConstitutionModifier();
        return hitPoints.getMaximum() + (hitDiceCount * conDelta);
    }

    private int resolveHitDiceCount(HitPointsDefinition hitPoints) {
        return Optional.ofNullable(hitPoints.getHitDice())
                .map(HitDice::getCount)
                .orElseGet(() -> inferHitDiceCount(hitPoints.getFormula()));
    }

    private int resolveHitDieSize(HitPointsDefinition hitPoints) {
        return Optional.ofNullable(hitPoints.getHitDice())
                .map(HitDice::getDieSize)
                .orElseGet(() -> inferHitDieSize(hitPoints.getFormula()));
    }

    private int inferHitDiceCount(String formula) {
        if (formula == null || formula.isBlank()) {
            return 1;
        }

        String normalized = normalizeFormula(formula);
        int dIndex = normalized.toLowerCase(Locale.ROOT).indexOf('d');
        if (dIndex <= 0) {
            return 1;
        }

        try {
            return Integer.parseInt(normalized.substring(0, dIndex).trim());
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private int inferHitDieSize(String formula) {
        if (formula == null || formula.isBlank()) {
            return 0;
        }

        String normalized = normalizeFormula(formula).toLowerCase(Locale.ROOT);
        int dIndex = normalized.indexOf('d');
        if (dIndex < 0 || dIndex == normalized.length() - 1) {
            return 0;
        }

        int endIndex = normalized.length();
        for (int index = dIndex + 1; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (character == '+' || character == '-') {
                endIndex = index;
                break;
            }
        }

        try {
            return Integer.parseInt(normalized.substring(dIndex + 1, endIndex));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String normalizeFormula(String formula) {
        String normalized = formula.trim().replace(" ", "");
        while (normalized.startsWith("(") && normalized.endsWith(")") && normalized.length() > 1) {
            normalized = normalized.substring(1, normalized.length() - 1).trim().replace(" ", "");
        }
        return normalized;
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
        return templateType == null ? baseAlignment : Alignment.NG;
    }

    private List<String> resolveSubtypes(List<String> baseSubtypes, SummonTemplateType templateType) {
        List<String> subtypes = new ArrayList<>(baseSubtypes == null ? List.of() : baseSubtypes);
        if (templateType == SummonTemplateType.CHTHONIC) {
            appendIfMissing(subtypes, "earth");
        }
        if (templateType == SummonTemplateType.FIERY) {
            appendIfMissing(subtypes, "fire");
        }
        return List.copyOf(subtypes);
    }

    private List<String> resolveSpecialAttacks(List<String> specialAttacks,
                                              SummonTemplateType templateType,
                                              int hitDiceCount,
                                              int charismaModifier) {
        List<String> resolved = new ArrayList<>(specialAttacks == null ? List.of() : specialAttacks);
        if (templateType == SummonTemplateType.CELESTIAL) {
            appendCalculatedSmite(resolved, "evil", charismaModifier, hitDiceCount);
        } else if (templateType == SummonTemplateType.ENTROPIC) {
            appendCalculatedSmite(resolved, "law", charismaModifier, hitDiceCount);
        } else if (templateType == SummonTemplateType.RESOLUTE) {
            appendCalculatedSmite(resolved, "chaos", charismaModifier, hitDiceCount);
        }
        return List.copyOf(resolved);
    }

    private List<com.pathfinder.summons.domain.model.SpecialDefense> resolveSpecialDefenses(CreatureTemplate template,
                                                                                            SummonTemplateType templateType,
                                                                                            int hitDiceCount,
                                                                                            String challengeRating) {
        List<com.pathfinder.summons.domain.model.SpecialDefense> specialDefenses = template.getSpecialDefenses();
        List<com.pathfinder.summons.domain.model.SpecialDefense> resolved = new ArrayList<>(specialDefenses == null ? List.of() : specialDefenses);
        if (templateType == SummonTemplateType.CHTHONIC) {
            String damageReduction = resolveChthonicDamageReduction(hitDiceCount);
            if (damageReduction != null) {
                resolved.add(damageReduction(damageReduction, "Template damage reduction"));
            }
            resolved.add(com.pathfinder.summons.domain.model.SpecialDefense.builder()
                    .type(com.pathfinder.summons.domain.model.SpecialDefenseType.RESISTANCE)
                    .value("acid " + resolveChthonicAcidResistance(hitDiceCount))
                    .notes("Template resistance")
                    .build());
        } else if (templateType == SummonTemplateType.FIERY) {
            resolved.add(com.pathfinder.summons.domain.model.SpecialDefense.builder()
                    .type(com.pathfinder.summons.domain.model.SpecialDefenseType.IMMUNITY)
                    .value("fire")
                    .notes("Granted by the fire subtype")
                    .build());
            resolved.add(com.pathfinder.summons.domain.model.SpecialDefense.builder()
                    .type(com.pathfinder.summons.domain.model.SpecialDefenseType.VULNERABILITY)
                    .value("cold")
                    .notes("Granted by the fire subtype")
                    .build());
        } else if (templateType == SummonTemplateType.CELESTIAL) {
            resolved.addAll(resolveCelestialDefenses(hitDiceCount, challengeRating));
        } else if (templateType == SummonTemplateType.ENTROPIC) {
            resolved.addAll(resolveEntropicDefenses(hitDiceCount, challengeRating));
        } else if (templateType == SummonTemplateType.RESOLUTE) {
            resolved.addAll(resolveResoluteDefenses(hitDiceCount, challengeRating));
        }
        return List.copyOf(resolved);
    }

    private List<com.pathfinder.summons.domain.model.SpecialDefense> resolveCelestialDefenses(int hitDiceCount, String challengeRating) {
        List<com.pathfinder.summons.domain.model.SpecialDefense> defenses = new ArrayList<>();
        int resistance = resolveCelestialResistance(hitDiceCount);
        String damageReduction = resolveCelestialDamageReduction(hitDiceCount);
        if (damageReduction != null) {
            defenses.add(damageReduction(damageReduction, "Template damage reduction"));
        }
        defenses.add(resistance("cold " + resistance));
        defenses.add(resistance("acid " + resistance));
        defenses.add(resistance("electricity " + resistance));
        addSpellResistance(defenses, challengeRating);
        return defenses;
    }

    private List<com.pathfinder.summons.domain.model.SpecialDefense> resolveEntropicDefenses(int hitDiceCount, String challengeRating) {
        List<com.pathfinder.summons.domain.model.SpecialDefense> defenses = new ArrayList<>();
        int resistance = resolveTemplateResistance(hitDiceCount);
        String damageReduction = resolveEntropicDamageReduction(hitDiceCount);
        if (damageReduction != null) {
            defenses.add(damageReduction(damageReduction, "Template damage reduction"));
        }
        defenses.add(resistance("acid " + resistance));
        defenses.add(resistance("fire " + resistance));
        addSpellResistance(defenses, challengeRating);
        return defenses;
    }

    private List<com.pathfinder.summons.domain.model.SpecialDefense> resolveResoluteDefenses(int hitDiceCount, String challengeRating) {
        List<com.pathfinder.summons.domain.model.SpecialDefense> defenses = new ArrayList<>();
        int resistance = resolveTemplateResistance(hitDiceCount);
        String damageReduction = resolveResoluteDamageReduction(hitDiceCount);
        if (damageReduction != null) {
            defenses.add(damageReduction(damageReduction, "Template damage reduction"));
        }
        defenses.add(resistance("acid " + resistance));
        defenses.add(resistance("cold " + resistance));
        defenses.add(resistance("fire " + resistance));
        addSpellResistance(defenses, challengeRating);
        return defenses;
    }

    private String resolveChthonicDamageReduction(int hitDiceCount) {
        if (hitDiceCount >= 11) {
            return "5/—";
        }
        if (hitDiceCount >= 5) {
            return "3/—";
        }
        return null;
    }

    private int resolveChthonicAcidResistance(int hitDiceCount) {
        if (hitDiceCount >= 11) {
            return 20;
        }
        if (hitDiceCount >= 5) {
            return 15;
        }
        return 10;
    }

    private int resolveCelestialResistance(int hitDiceCount) {
        if (hitDiceCount >= 11) {
            return 15;
        }
        if (hitDiceCount >= 5) {
            return 10;
        }
        return 5;
    }

    private int resolveTemplateResistance(int hitDiceCount) {
        if (hitDiceCount >= 11) {
            return 15;
        }
        if (hitDiceCount >= 5) {
            return 10;
        }
        return 5;
    }

    private String resolveCelestialDamageReduction(int hitDiceCount) {
        if (hitDiceCount >= 11) {
            return "10/evil";
        }
        if (hitDiceCount >= 5) {
            return "5/evil";
        }
        return null;
    }

    private String resolveEntropicDamageReduction(int hitDiceCount) {
        if (hitDiceCount >= 11) {
            return "10/lawful";
        }
        if (hitDiceCount >= 5) {
            return "5/lawful";
        }
        return null;
    }

    private String resolveResoluteDamageReduction(int hitDiceCount) {
        if (hitDiceCount >= 11) {
            return "10/chaotic";
        }
        if (hitDiceCount >= 5) {
            return "5/chaotic";
        }
        return null;
    }

    private void addSpellResistance(List<com.pathfinder.summons.domain.model.SpecialDefense> defenses, String challengeRating) {
        BigDecimal cr = parseChallengeRating(challengeRating);
        if (cr.compareTo(BigDecimal.ZERO) > 0) {
            int spellResistance = cr.add(BigDecimal.valueOf(5)).setScale(0, RoundingMode.DOWN).intValue();
            defenses.add(spellResistance(String.valueOf(spellResistance)));
        }
    }

    private BigDecimal parseChallengeRating(String challengeRating) {
        if (challengeRating == null || challengeRating.isBlank()) {
            return BigDecimal.ZERO;
        }

        String normalized = challengeRating.trim().toUpperCase(Locale.ROOT).replace("CR", "").trim();
        if (normalized.contains("/")) {
            String[] parts = normalized.split("/", 2);
            try {
                BigDecimal numerator = new BigDecimal(parts[0].trim());
                BigDecimal denominator = new BigDecimal(parts[1].trim());
                if (denominator.compareTo(BigDecimal.ZERO) == 0) {
                    return BigDecimal.ZERO;
                }
                return numerator.divide(denominator, 10, RoundingMode.HALF_UP);
            } catch (NumberFormatException | ArithmeticException ex) {
                return BigDecimal.ZERO;
            }
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private com.pathfinder.summons.domain.model.SpecialDefense spellResistance(String value) {
        return com.pathfinder.summons.domain.model.SpecialDefense.builder()
                .type(com.pathfinder.summons.domain.model.SpecialDefenseType.SPELL_RESISTANCE)
                .value(value)
                .notes("Template spell resistance")
                .build();
    }

    private com.pathfinder.summons.domain.model.SpecialDefense damageReduction(String value, String notes) {
        return com.pathfinder.summons.domain.model.SpecialDefense.builder()
                .type(com.pathfinder.summons.domain.model.SpecialDefenseType.DAMAGE_REDUCTION)
                .value(value)
                .notes(notes)
                .build();
    }

    private void appendCalculatedSmite(List<String> resolved, String target, int charismaModifier, int hitDiceCount) {
        String genericSmite = "Smite " + target + " 1/day (swift action)";
        resolved.removeIf(value -> value != null && value.startsWith("Smite " + target));
        resolved.add(genericSmite + " — attack " + formatSigned(charismaModifier) + ", damage +" + hitDiceCount);
    }

    private String buildFullStatBlock(String displayName,
                                      Alignment alignment,
                                      CreatureSize size,
                                      String creatureType,
                                      List<String> subtypes,
                                      int initiative,
                                      List<String> senses,
                                      int perception,
                                      ArmorClass armorClass,
                                      int maxHitPoints,
                                      SavingThrows savingThrows,
                                      List<Speed> speeds,
                                      List<Attack> attacks,
                                      String space,
                                      String reach,
                                      List<String> specialAttacks,
                                      List<com.pathfinder.summons.domain.model.SpecialDefense> specialDefenses) {
        String sensesText = String.join(", ", senses);
        String speedText = formatSpeeds(speeds);
        String attackText = formatAttacks(attacks);
        String subtypeText = subtypes == null || subtypes.isEmpty() ? "" : " (" + String.join(", ", subtypes) + ")";
        return displayName + "\n"
                + alignment + " " + toDisplaySize(size) + " " + creatureType + subtypeText + "\n"
                + "Init +" + initiative + "; Senses " + sensesText + "; Perception +" + perception + "\n"
                + "AC " + armorClass.getNormal() + ", touch " + armorClass.getTouch() + ", flat-footed " + armorClass.getFlatFooted() + " (" + armorClass.getDetail() + ")\n"
                + "hp " + maxHitPoints + "\n"
                + "Fort +" + savingThrows.getFortitude() + ", Ref +" + savingThrows.getReflex() + ", Will +" + savingThrows.getWill() + "\n"
                + speedText + "\n"
                + attackText + "\n"
                + "Space " + space + "; Reach " + reach + "\n"
                + "Special Attacks " + joinOrDash(specialAttacks) + "\n"
                + "Special Defenses " + joinSpecialDefenses(specialDefenses);
    }

    private String joinOrDash(List<String> values) {
        if (values.isEmpty()) {
            return "—";
        }

        return String.join(", ", values);
    }

    private String joinSpecialDefenses(List<com.pathfinder.summons.domain.model.SpecialDefense> specialDefenses) {
        if (specialDefenses.isEmpty()) {
            return "—";
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (com.pathfinder.summons.domain.model.SpecialDefense specialDefense : specialDefenses) {
            StringBuilder builder = new StringBuilder(formatSpecialDefenseType(specialDefense.getType()));
            if (specialDefense.getValue() != null && !specialDefense.getValue().isBlank()) {
                builder.append(' ').append(specialDefense.getValue());
            }
            joiner.add(builder.toString());
        }
        return joiner.toString();
    }

    private String formatSpecialDefenseType(com.pathfinder.summons.domain.model.SpecialDefenseType type) {
        return switch (type) {
            case DAMAGE_REDUCTION -> "DR";
            case RESISTANCE -> "Resistance";
            case IMMUNITY -> "Immune";
            case SPELL_RESISTANCE -> "SR";
            case VULNERABILITY -> "Vulnerability";
            case OTHER -> "";
        };
    }

    private String toDisplaySize(CreatureSize size) {
        String raw = size.name().toLowerCase(Locale.ROOT);
        return raw.substring(0, 1).toUpperCase(Locale.ROOT) + raw.substring(1);
    }

    private int resolveCombatManeuverBonus(List<Attack> attacks) {
        return attacks.stream()
                .filter(attack -> attack.getAttackType() == AttackType.MELEE)
                .mapToInt(Attack::getAttackBonus)
                .max()
                .orElseGet(() -> attacks.stream()
                        .mapToInt(Attack::getAttackBonus)
                        .max()
                        .orElse(0));
    }

    private com.pathfinder.summons.domain.model.SpecialDefense resistance(String value) {
        return com.pathfinder.summons.domain.model.SpecialDefense.builder()
                .type(com.pathfinder.summons.domain.model.SpecialDefenseType.RESISTANCE)
                .value(value)
                .notes("Template resistance")
                .build();
    }

    private <T> void appendIfMissing(List<T> values, T value) {
        if (!values.contains(value)) {
            values.add(value);
        }
    }
}
