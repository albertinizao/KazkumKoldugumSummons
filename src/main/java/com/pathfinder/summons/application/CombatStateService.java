package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.ActiveSummonGroup;
import com.pathfinder.summons.domain.model.ActiveSummonInstance;
import com.pathfinder.summons.domain.model.ActiveSummonStatus;
import com.pathfinder.summons.domain.model.CombatState;
import com.pathfinder.summons.domain.model.ConfigurationSummary;
import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.DailyUses;
import com.pathfinder.summons.domain.model.GroupAttackRollResponse;
import com.pathfinder.summons.domain.model.GroupAttackRollResult;
import com.pathfinder.summons.domain.model.GroupSavingThrowsRollResponse;
import com.pathfinder.summons.domain.model.GroupSavingThrowsRollResult;
import com.pathfinder.summons.domain.model.RollDisplay;
import com.pathfinder.summons.domain.model.RollDisplayType;
import com.pathfinder.summons.domain.model.SummonShortcut;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import com.pathfinder.summons.domain.repository.CombatStateRepository;
import com.pathfinder.summons.domain.service.CreatureResolver;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@Transactional
public class CombatStateService implements CombatStateUseCase {

    private static final int DEFAULT_DAILY_MAXIMUM = 6;
    private static final int DEFAULT_DAILY_REMAINING = 4;
    private static final int RECENT_SHORTCUT_LIMIT = 5;

    private final CombatStateRepository combatStateRepository;
    private final CreatureCatalogService creatureCatalogService;
    private final SummonerConfigurationService configurationService;
    private final CreatureResolver creatureResolver;
    private final SummonQuantityCalculator summonQuantityCalculator;
    private final CombatRollService combatRollService;

    public CombatStateService(CombatStateRepository combatStateRepository,
                              CreatureCatalogService creatureCatalogService,
                              SummonerConfigurationService configurationService,
                              CreatureResolver creatureResolver,
                              SummonQuantityCalculator summonQuantityCalculator,
                              CombatRollService combatRollService) {
        this.combatStateRepository = combatStateRepository;
        this.creatureCatalogService = creatureCatalogService;
        this.configurationService = configurationService;
        this.creatureResolver = creatureResolver;
        this.summonQuantityCalculator = summonQuantityCalculator;
        this.combatRollService = combatRollService;
    }

    @Override
    public CombatState getCombatState() {
        CombatState state = loadOrCreateState();
        return refreshConfiguration(state);
    }

    @Override
    public CombatState summon(String creatureId, SummonTemplateType templateType) {
        CreatureTemplate template = creatureCatalogService.getCreatureTemplate(creatureId);
        validateSummonLevel(template);
        return summonInternal(template, templateType, null);
    }

    public CombatState summonFromShortcut(String shortcutId, String source, SummonTemplateType selectedTemplate) {
        CombatState state = loadOrCreateState();
        SummonShortcut shortcut = findShortcut(state, shortcutId, source);
        CreatureTemplate template = creatureCatalogService.getCreatureTemplate(shortcut.getCreatureTemplateId());
        validateSummonLevel(template);
        SummonTemplateType templateType = selectedTemplate != null ? selectedTemplate : shortcut.getSelectedTemplate();
        return summonInternal(template, templateType, shortcut);
    }

    @Override
    public CombatState damageInstance(String groupId, String instanceId, int amount) {
        CombatState state = loadOrCreateState();
        return updateInstance(state, groupId, instanceId, instance -> instance.damage(amount));
    }

    @Override
    public CombatState healInstance(String groupId, String instanceId, int amount) {
        CombatState state = loadOrCreateState();
        return updateInstance(state, groupId, instanceId, instance -> instance.heal(amount));
    }

    @Override
    public CombatState removeInstance(String groupId, String instanceId) {
        CombatState state = loadOrCreateState();
        List<ActiveSummonGroup> updatedGroups = new ArrayList<>(state.getActiveGroups());
        int groupIndex = findGroupIndex(updatedGroups, groupId);
        if (groupIndex < 0) {
            throw notFound("No existe el grupo activo " + groupId);
        }

        ActiveSummonGroup group = updatedGroups.get(groupIndex);
        List<ActiveSummonInstance> remainingInstances = group.getInstances().stream()
                .filter(instance -> !Objects.equals(instance.getId(), instanceId))
                .collect(Collectors.toCollection(ArrayList::new));

        if (remainingInstances.size() == group.getInstances().size()) {
            throw notFound("No existe la instancia " + instanceId + " en el grupo " + groupId);
        }

        if (remainingInstances.isEmpty()) {
            updatedGroups.remove(groupIndex);
        } else {
            updatedGroups.set(groupIndex, group.toBuilder().instances(List.copyOf(remainingInstances)).build());
        }

        CombatState updated = state.toBuilder().activeGroups(List.copyOf(updatedGroups)).build();
        return saveWithCurrentConfiguration(updated);
    }

    @Override
    public CombatState clearSummons() {
        CombatState state = loadOrCreateState();
        CombatState updated = state.toBuilder()
                .activeGroups(List.of())
                .build();
        return saveWithCurrentConfiguration(updated);
    }

    @Override
    public CombatState updateMaxSummonLevel(int newLevel) {
        int dailyUsesMaximum = configurationService.getConfiguration().getDailyUses().getMaximum();
        configurationService.updateConfiguration(newLevel, dailyUsesMaximum);
        CombatState state = loadOrCreateState();
        return refreshConfiguration(state);
    }

    @Override
    public CombatState updateDailyUses(int maximum, int remaining) {
        CombatState state = loadOrCreateState();
        if (maximum < 0 || remaining < 0 || remaining > maximum) {
            throw new ResponseStatusException(BAD_REQUEST, "Usos diarios inválidos");
        }
        CombatState updated = state.toBuilder()
                .dailyUses(new DailyUses(maximum, remaining))
                .build();
        return saveWithCurrentConfiguration(updated);
    }

    @Override
    public CombatState clearLastRollResult() {
        CombatState state = loadOrCreateState();
        CombatState updated = state.toBuilder()
                .lastRollResult(null)
                .build();
        return saveWithCurrentConfiguration(updated);
    }

    @Override
    public GroupAttackRollResponse rollGroupAttacks(String groupId) {
        CombatState state = loadOrCreateState();
        ActiveSummonGroup group = findGroup(state, groupId);
        GroupAttackRollResult rollResult = combatRollService.rollGroupAttacks(group);
        CombatState updated = saveLastRollResult(state, rollResult);
        return new GroupAttackRollResponse(rollResult, updated);
    }

    @Override
    public GroupSavingThrowsRollResponse rollGroupSavingThrows(String groupId) {
        CombatState state = loadOrCreateState();
        ActiveSummonGroup group = findGroup(state, groupId);
        GroupSavingThrowsRollResult rollResult = combatRollService.rollGroupSavingThrows(group);
        CombatState updated = saveLastRollResult(state, rollResult);
        return new GroupSavingThrowsRollResponse(rollResult, updated);
    }

    @Override
    public CombatState incrementDailyUses(int amount) {
        CombatState state = loadOrCreateState();
        CombatState updated = state.toBuilder()
                .dailyUses(state.getDailyUses().increase(amount))
                .build();
        return saveWithCurrentConfiguration(updated);
    }

    @Override
    public CombatState decrementDailyUses(int amount) {
        CombatState state = loadOrCreateState();
        CombatState updated = state.toBuilder()
                .dailyUses(state.getDailyUses().decrease(amount))
                .build();
        return saveWithCurrentConfiguration(updated);
    }

    @Override
    public CombatState resetDailyUses() {
        CombatState state = loadOrCreateState();
        CombatState updated = state.toBuilder()
                .dailyUses(state.getDailyUses().reset())
                .build();
        return saveWithCurrentConfiguration(updated);
    }

    public CombatState clearCombatState() {
        combatStateRepository.clear();
        CombatState defaultState = createDefaultState();
        combatStateRepository.saveCombatState(defaultState);
        return defaultState;
    }

    private CombatState summonInternal(CreatureTemplate template,
                                       SummonTemplateType templateType,
                                       SummonShortcut sourceShortcut) {
        CombatState state = loadOrCreateState();
        ConfigurationSummary configuration = configurationService.getConfigurationSummary();
        if (template.getSummonLevel() > configuration.getMaxSummonMonsterLevel()) {
            throw new ResponseStatusException(BAD_REQUEST, "La criatura excede el nivel máximo de invocación");
        }

        var resolvedCreature = creatureResolver.resolve(template, templateType, configurationService.getConfiguration());
        var quantityPlan = summonQuantityCalculator.describe(configurationService.getConfiguration(), template.getSummonLevel());
        int quantity = rollQuantity(quantityPlan.formula());
        String shortcutKey = shortcutKey(template.getId(), templateType);

        List<ActiveSummonGroup> updatedGroups = new ArrayList<>(state.getActiveGroups());
        int existingGroupIndex = findGroupIndex(updatedGroups, resolvedCreature.getId());
        ActiveSummonGroup updatedGroup;
        List<String> createdInstanceIds = new ArrayList<>();

        if (existingGroupIndex >= 0) {
            ActiveSummonGroup existingGroup = updatedGroups.get(existingGroupIndex);
            List<ActiveSummonInstance> instances = new ArrayList<>(existingGroup.getInstances());
            int nextDisplayNumber = instances.size() + 1;
            for (int index = 0; index < quantity; index++) {
                ActiveSummonInstance instance = ActiveSummonInstance.create(
                        UUID.randomUUID().toString(),
                        nextDisplayNumber + index,
                        resolvedCreature.getDisplayName() + " " + (nextDisplayNumber + index),
                        resolvedCreature.getMaxHitPoints());
                instances.add(instance);
                createdInstanceIds.add(instance.getId());
            }
            updatedGroup = existingGroup.toBuilder().instances(List.copyOf(instances)).build();
            updatedGroups.set(existingGroupIndex, updatedGroup);
        } else {
            List<ActiveSummonInstance> instances = new ArrayList<>();
            for (int index = 0; index < quantity; index++) {
                ActiveSummonInstance instance = ActiveSummonInstance.create(
                        UUID.randomUUID().toString(),
                        index + 1,
                        resolvedCreature.getDisplayName() + " " + (index + 1),
                        resolvedCreature.getMaxHitPoints());
                instances.add(instance);
                createdInstanceIds.add(instance.getId());
            }
            updatedGroup = ActiveSummonGroup.builder()
                    .id(resolvedCreature.getId())
                    .resolvedCreature(resolvedCreature)
                    .instances(List.copyOf(instances))
                    .build();
            updatedGroups.add(updatedGroup);
        }

        DailyUses updatedUses = state.getDailyUses().consumeOne();
        SummonShortcut updatedShortcut = SummonShortcut.builder()
                .id(shortcutKey)
                .creatureTemplateId(template.getId())
                .selectedTemplate(templateType)
                .displayName(resolvedCreature.getDisplayName())
                .usageCount(sourceShortcut == null ? 1 : sourceShortcut.getUsageCount() + 1)
                .lastUsedAt(LocalDateTime.now())
                .build();

        List<SummonShortcut> recentlyUsed = upsertRecentShortcut(state.getRecentlyUsedSummons(), updatedShortcut);
        List<SummonShortcut> mostUsed = upsertMostUsedShortcut(state.getMostUsedSummons(), updatedShortcut);

        RollDisplay rollDisplay = RollDisplay.builder()
                .id(UUID.randomUUID().toString())
                .type(RollDisplayType.SUMMON_QUANTITY)
                .title("Cantidad invocada: " + resolvedCreature.getDisplayName())
                .createdAt(LocalDateTime.now())
                .content(quantityPlan.formula() + " = " + quantity)
                .build();

        CombatState updated = state.toBuilder()
                .activeGroups(List.copyOf(updatedGroups))
                .dailyUses(updatedUses)
                .lastRollResult(rollDisplay)
                .recentlyUsedSummons(recentlyUsed)
                .mostUsedSummons(mostUsed)
                .build();

        return saveWithCurrentConfiguration(updated);
    }

    private CombatState updateInstance(CombatState state,
                                       String groupId,
                                       String instanceId,
                                       InstanceUpdater updater) {
        List<ActiveSummonGroup> updatedGroups = new ArrayList<>(state.getActiveGroups());
        int groupIndex = findGroupIndex(updatedGroups, groupId);
        if (groupIndex < 0) {
            throw notFound("No existe el grupo activo " + groupId);
        }

        ActiveSummonGroup group = updatedGroups.get(groupIndex);
        List<ActiveSummonInstance> updatedInstances = new ArrayList<>();
        boolean found = false;
        for (ActiveSummonInstance instance : group.getInstances()) {
            if (Objects.equals(instance.getId(), instanceId)) {
                updatedInstances.add(updater.update(instance));
                found = true;
            } else {
                updatedInstances.add(instance);
            }
        }

        if (!found) {
            throw notFound("No existe la instancia " + instanceId + " en el grupo " + groupId);
        }

        ActiveSummonGroup updatedGroup = group.toBuilder()
                .instances(List.copyOf(updatedInstances))
                .build();
        updatedGroups.set(groupIndex, updatedGroup);
        CombatState updated = state.toBuilder().activeGroups(List.copyOf(updatedGroups)).build();
        return saveWithCurrentConfiguration(updated);
    }

    private List<SummonShortcut> upsertRecentShortcut(List<SummonShortcut> existing, SummonShortcut shortcut) {
        return existing.stream()
                .filter(item -> !Objects.equals(item.getId(), shortcut.getId()))
                .collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), list -> {
                    list.add(0, shortcut);
                    return list.stream().limit(RECENT_SHORTCUT_LIMIT).toList();
                }));
    }

    private List<SummonShortcut> upsertMostUsedShortcut(List<SummonShortcut> existing, SummonShortcut shortcut) {
        ArrayList<SummonShortcut> updated = existing.stream()
                .filter(item -> !Objects.equals(item.getId(), shortcut.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        updated.add(shortcut);
        updated.sort(Comparator.comparingInt(SummonShortcut::getUsageCount).reversed()
                .thenComparing(SummonShortcut::getLastUsedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return updated.stream().limit(RECENT_SHORTCUT_LIMIT).toList();
    }

    private SummonShortcut findShortcut(CombatState state, String shortcutId, String source) {
        List<SummonShortcut> shortcuts = switch (normalizeSource(source)) {
            case "RECENT" -> state.getRecentlyUsedSummons();
            case "MOST_USED" -> state.getMostUsedSummons();
            default -> throw new ResponseStatusException(BAD_REQUEST, "Source de invocación inválida");
        };

        return shortcuts.stream()
                .filter(shortcut -> Objects.equals(shortcut.getId(), shortcutId))
                .findFirst()
                .orElseThrow(() -> notFound("No existe el acceso rápido " + shortcutId));
    }

    private String normalizeSource(String source) {
        return source == null ? "" : source.trim().toUpperCase(Locale.ROOT);
    }

    private int rollQuantity(String formula) {
        if (formula == null || formula.isBlank() || "1".equals(formula.trim())) {
            return 1;
        }

        String normalized = formula.replace(" ", "").toLowerCase(Locale.ROOT);
        int dIndex = normalized.indexOf('d');
        if (dIndex <= 0) {
            return 1;
        }

        int sides = Integer.parseInt(normalized.substring(dIndex + 1, normalized.contains("+") ? normalized.indexOf('+') : normalized.length()));
        int bonus = normalized.contains("+") ? Integer.parseInt(normalized.substring(normalized.indexOf('+') + 1)) : 0;
        int rolled = ThreadLocalRandom.current().nextInt(1, sides + 1);
        return rolled + bonus;
    }

    private CombatState loadOrCreateState() {
        CombatState state = combatStateRepository.getCombatState();
        if (state == null) {
            state = createDefaultState();
            combatStateRepository.saveCombatState(state);
        }
        return state;
    }

    private CombatState createDefaultState() {
        return CombatState.builder()
                .activeGroups(List.of())
                .dailyUses(new DailyUses(DEFAULT_DAILY_MAXIMUM, DEFAULT_DAILY_REMAINING))
                .configuration(configurationService.getConfigurationSummary())
                .lastRollResult(null)
                .recentlyUsedSummons(List.of())
                .mostUsedSummons(List.of())
                .build();
    }

    private CombatState saveWithCurrentConfiguration(CombatState state) {
        CombatState updated = refreshConfiguration(state);
        combatStateRepository.saveCombatState(updated);
        return updated;
    }

    private CombatState saveLastRollResult(CombatState state, GroupAttackRollResult rollResult) {
        CombatState updated = state.toBuilder()
                .lastRollResult(RollDisplay.builder()
                        .id(rollResult.id())
                        .type(rollResult.type())
                        .title(rollResult.title())
                        .createdAt(rollResult.createdAt())
                        .content(rollResult.displayText())
                        .build())
                .build();
        return saveWithCurrentConfiguration(updated);
    }

    private CombatState saveLastRollResult(CombatState state, GroupSavingThrowsRollResult rollResult) {
        CombatState updated = state.toBuilder()
                .lastRollResult(RollDisplay.builder()
                        .id(rollResult.id())
                        .type(rollResult.type())
                        .title(rollResult.title())
                        .createdAt(rollResult.createdAt())
                        .content(rollResult.displayText())
                        .build())
                .build();
        return saveWithCurrentConfiguration(updated);
    }

    private CombatState refreshConfiguration(CombatState state) {
        ConfigurationSummary currentConfiguration = configurationService.getConfigurationSummary();
        if (Objects.equals(state.getConfiguration(), currentConfiguration)) {
            return state;
        }

        CombatState updated = state.toBuilder()
                .configuration(currentConfiguration)
                .build();
        combatStateRepository.saveCombatState(updated);
        return updated;
    }

    private int findGroupIndex(List<ActiveSummonGroup> groups, String groupId) {
        for (int index = 0; index < groups.size(); index++) {
            if (Objects.equals(groups.get(index).getId(), groupId)) {
                return index;
            }
        }
        return -1;
    }

    private ActiveSummonGroup findGroup(CombatState state, String groupId) {
        return state.getActiveGroups().stream()
                .filter(group -> Objects.equals(group.getId(), groupId))
                .findFirst()
                .orElseThrow(() -> notFound("No existe el grupo activo " + groupId));
    }

    private void validateSummonLevel(CreatureTemplate template) {
        int maxSummonMonsterLevel = configurationService.getConfiguration().getMaxSummonMonsterLevel();
        if (template.getSummonLevel() > maxSummonMonsterLevel) {
            throw new ResponseStatusException(BAD_REQUEST, "La criatura excede el nivel máximo de invocación");
        }
    }

    private String shortcutKey(String creatureTemplateId, SummonTemplateType templateType) {
        return creatureTemplateId + ":" + (templateType == null ? "NONE" : templateType.name());
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(NOT_FOUND, message);
    }

    @FunctionalInterface
    private interface InstanceUpdater {
        ActiveSummonInstance update(ActiveSummonInstance instance);
    }
}
