package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.CombatState;
import com.pathfinder.summons.domain.model.DailyUses;
import com.pathfinder.summons.domain.model.SummonerConfiguration;
import com.pathfinder.summons.domain.repository.SummonerConfigurationRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class SummonerConfigurationService {

    private final SummonerConfigurationRepository repository;
    private final SummonQuantityCalculator quantityCalculator;

    public SummonerConfigurationService(SummonerConfigurationRepository repository,
                                        SummonQuantityCalculator quantityCalculator) {
        this.repository = repository;
        this.quantityCalculator = quantityCalculator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureDefaultConfiguration() {
        normalizeConfiguration(repository.findById(SummonerConfiguration.SINGLETON_ID)
                .orElseGet(() -> repository.save(SummonerConfiguration.defaultConfiguration())));
    }

    public SummonerConfiguration getConfiguration() {
        return normalizeConfiguration(repository.findById(SummonerConfiguration.SINGLETON_ID)
                .orElseGet(() -> repository.save(SummonerConfiguration.defaultConfiguration())));
    }

    public SummonerConfiguration updateMaxSummonMonsterLevel(@Min(0) int maxSummonMonsterLevel) {
        SummonerConfiguration configuration = getConfiguration();
        configuration.setMaxSummonMonsterLevel(maxSummonMonsterLevel);
        return repository.save(configuration);
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
                .configuration(configuration)
                .lastRollResult(null)
                .build();
    }

    public boolean isCreatureAvailable(int creatureSummonLevel) {
        return quantityCalculator.isAvailable(getConfiguration(), creatureSummonLevel);
    }

    public SummonQuantityCalculator.SummonQuantityPlan calculateQuantityFor(int creatureSummonLevel) {
        return quantityCalculator.describe(getConfiguration(), creatureSummonLevel);
    }

    private SummonerConfiguration normalizeConfiguration(SummonerConfiguration configuration) {
        DailyUses normalized = DailyUses.normalize(configuration.getDailyUsesMaximum(), configuration.getDailyUsesRemaining());
        if (normalized.getMaximum() != configuration.getDailyUsesMaximum()
                || normalized.getRemaining() != configuration.getDailyUsesRemaining()) {
            configuration.setDailyUses(normalized);
            return repository.save(configuration);
        }

        return configuration;
    }
}
