package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.ArmorClass;
import com.pathfinder.summons.domain.model.Attack;
import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.ResolvedCreature;
import com.pathfinder.summons.domain.model.SavingThrows;
import com.pathfinder.summons.domain.model.Speed;
import com.pathfinder.summons.domain.model.SpeedType;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import com.pathfinder.summons.domain.model.SummonerConfiguration;
import com.pathfinder.summons.domain.repository.CreatureTemplateRepository;
import com.pathfinder.summons.domain.service.CreatureResolver;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CreatureCatalogService {

    private final CreatureTemplateRepository repository;
    private final CreatureResolver creatureResolver;

    public CreatureCatalogService(CreatureTemplateRepository repository, CreatureResolver creatureResolver) {
        this.repository = repository;
        this.creatureResolver = creatureResolver;
    }

    public List<CreatureTemplate> findCreatures(String query,
                                                Integer summonLevel,
                                                Integer maxSummonLevel,
                                                SummonTemplateType template,
                                                @Min(1) int limit,
                                                @Min(0) int offset) {
        return filterCreatures(query, summonLevel, maxSummonLevel, template).stream()
                .skip(Math.max(0, offset))
                .limit(Math.max(1, limit))
                .toList();
    }

    public int countCreatures(String query,
                              Integer summonLevel,
                              Integer maxSummonLevel,
                              SummonTemplateType template) {
        return filterCreatures(query, summonLevel, maxSummonLevel, template).size();
    }

    private List<CreatureTemplate> filterCreatures(String query,
                                                   Integer summonLevel,
                                                   Integer maxSummonLevel,
                                                   SummonTemplateType template) {
        return repository.findAll().stream()
                .filter(creature -> matchesQuery(creature, query))
                .filter(creature -> summonLevel == null || creature.getSummonLevel() == summonLevel)
                .filter(creature -> maxSummonLevel == null || creature.getSummonLevel() <= maxSummonLevel)
                .filter(creature -> template == null || creature.getAllowedTemplates().contains(template))
                .toList();
    }

    public CreatureTemplate getCreatureTemplate(String creatureTemplateId) {
        return repository.findById(creatureTemplateId)
                .orElseThrow(() -> new NoSuchElementException("No existe la criatura base " + creatureTemplateId));
    }

    public ResolvedCreature previewResolvedCreature(String creatureTemplateId, SummonTemplateType templateType) {
        CreatureTemplate creatureTemplate = getCreatureTemplate(creatureTemplateId);
        return creatureResolver.resolve(creatureTemplate, templateType, SummonerConfiguration.defaultConfiguration());
    }

    public CreatureCatalogItemSummary toSummary(CreatureTemplate creatureTemplate) {
        return new CreatureCatalogItemSummary(
                creatureTemplate.getArmorClass(),
                creatureTemplate.getHitPoints().getMaximum(),
                creatureTemplate.getSavingThrows(),
                formatSpeeds(creatureTemplate.getSpeeds()),
                formatAttacks(creatureTemplate.getAttacks())
        );
    }

    private boolean matchesQuery(CreatureTemplate creature, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return creature.getName().toLowerCase(Locale.ROOT).contains(normalized)
                || creature.getId().toLowerCase(Locale.ROOT).contains(normalized)
                || creature.getCreatureType().toLowerCase(Locale.ROOT).contains(normalized);
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
                .map(attack -> {
                    String attackType = attack.getAttackType().name().charAt(0) + attack.getAttackType().name().substring(1).toLowerCase(Locale.ROOT);
                    String attackLabel = attack.getQuantity() > 1
                            ? attack.getQuantity() + " " + pluralize(attack.getName().toLowerCase(Locale.ROOT))
                            : attack.getName().toLowerCase(Locale.ROOT);
                    String damage = attack.getDamageComponents().stream()
                            .map(component -> component.getFormula())
                            .collect(Collectors.joining(" + "));
                    return attackType + " " + attackLabel + " +" + attack.getAttackBonus() + " (" + damage + ")";
                })
                .collect(Collectors.joining(", "));
    }

    private String pluralize(String name) {
        return name.endsWith("s") ? name : name + "s";
    }

    public record CreatureCatalogItemSummary(
            ArmorClass armorClass,
            int maxHitPoints,
            SavingThrows savingThrows,
            String speedsText,
            String attacksText
    ) {
    }
}
