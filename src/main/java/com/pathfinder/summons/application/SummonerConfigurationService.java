package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.CombatState;
import com.pathfinder.summons.domain.model.ConfigurationSummary;
import com.pathfinder.summons.domain.model.DailyUses;
import com.pathfinder.summons.domain.model.SummonerConfiguration;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import com.pathfinder.summons.domain.repository.CombatStateRepository;
import com.pathfinder.summons.domain.repository.SummonerConfigurationRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SummonerConfigurationService {

    private final SummonerConfigurationRepository repository;
    private final CombatStateRepository combatStateRepository;
    private final SummonQuantityCalculator quantityCalculator;
    private final SummonerConfigurationSchemaMigrator schemaMigrator;

    public SummonerConfigurationService(SummonerConfigurationRepository repository,
                                        CombatStateRepository combatStateRepository,
                                        SummonQuantityCalculator quantityCalculator,
                                        SummonerConfigurationSchemaMigrator schemaMigrator) {
        this.repository = repository;
        this.combatStateRepository = combatStateRepository;
        this.quantityCalculator = quantityCalculator;
        this.schemaMigrator = schemaMigrator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureDefaultConfiguration() {
        schemaMigrator.migrate();
        normalizeConfiguration(repository.findById(SummonerConfiguration.SINGLETON_ID)
                .orElseGet(() -> repository.save(SummonerConfiguration.defaultConfiguration())));
    }

    public SummonerConfiguration getConfiguration() {
        return normalizeConfiguration(repository.findById(SummonerConfiguration.SINGLETON_ID)
                .orElseGet(() -> repository.save(SummonerConfiguration.defaultConfiguration())));
    }

    public SummonerConfiguration updateConfiguration(@Min(0) int maxSummonMonsterLevel, @Min(0) int dailyUsesMaximum) {
        SummonerConfiguration configuration = getConfiguration();
        configuration.setMaxSummonMonsterLevel(maxSummonMonsterLevel);
        configuration.setDailyUses(configuration.getDailyUses().updateMaximum(dailyUsesMaximum));
        SummonerConfiguration saved = repository.save(configuration);
        syncCombatStateDailyUses(saved.getDailyUses());
        return saved;
    }

    public DailyUses getDailyUses() {
        return getConfiguration().getDailyUses();
    }

    public DailyUses increaseDailyUses(int amount) {
        SummonerConfiguration configuration = getConfiguration();
        configuration.setDailyUses(configuration.getDailyUses().increase(amount));
        return repository.save(configuration).getDailyUses();
    }

    public DailyUses decreaseDailyUses(int amount) {
        SummonerConfiguration configuration = getConfiguration();
        configuration.setDailyUses(configuration.getDailyUses().decrease(amount));
        return repository.save(configuration).getDailyUses();
    }

    public DailyUses resetDailyUses() {
        SummonerConfiguration configuration = getConfiguration();
        configuration.setDailyUses(configuration.getDailyUses().reset());
        return repository.save(configuration).getDailyUses();
    }

    public CombatState snapshotCombatState() {
        SummonerConfiguration configuration = getConfiguration();
        return CombatState.builder()
                .activeGroups(List.of())
                .dailyUses(configuration.getDailyUses())
                .configuration(getConfigurationSummary())
                .lastRollResult(null)
                .build();
    }

    public boolean isCreatureAvailable(int creatureSummonLevel) {
        return quantityCalculator.isAvailable(getConfiguration(), creatureSummonLevel);
    }

    public SummonQuantityCalculator.SummonQuantityPlan calculateQuantityFor(int creatureSummonLevel) {
        return quantityCalculator.describe(getConfiguration(), creatureSummonLevel);
    }

    public ConfigurationSummary getConfigurationSummary() {
        SummonerConfiguration configuration = getConfiguration();
        return ConfigurationSummary.builder()
                .maxSummonMonsterLevel(configuration.getMaxSummonMonsterLevel())
                .availableTemplates(List.of(SummonTemplateType.values()))
                .build();
    }

    private SummonerConfiguration normalizeConfiguration(SummonerConfiguration configuration) {
        DailyUses normalized = DailyUses.normalize(configuration.getDailyUsesMaximum(), configuration.getDailyUsesRemaining());
        if (normalized.getMaximum() != configuration.getDailyUsesMaximum()
                || normalized.getRemaining() != configuration.getDailyUsesRemaining()) {
            configuration.setDailyUses(normalized);
            SummonerConfiguration saved = repository.save(configuration);
            syncCombatStateDailyUses(saved.getDailyUses());
            return saved;
        }

        return configuration;
    }

    private void syncCombatStateDailyUses(DailyUses dailyUses) {
        CombatState combatState = combatStateRepository.getCombatState();
        if (combatState == null) {
            return;
        }

        CombatState updated = combatState.toBuilder()
                .dailyUses(dailyUses)
                .build();
        combatStateRepository.saveCombatState(updated);
    }
}
