package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.SummonerConfiguration;
import com.pathfinder.summons.domain.model.ConfigurationSummary;
import com.pathfinder.summons.domain.model.SummonTemplateType;
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
        repository.findById(SummonerConfiguration.SINGLETON_ID)
                .orElseGet(() -> repository.save(SummonerConfiguration.defaultConfiguration()));
    }

    public SummonerConfiguration getConfiguration() {
        return repository.findById(SummonerConfiguration.SINGLETON_ID)
                .orElseGet(() -> repository.save(SummonerConfiguration.defaultConfiguration()));
    }

    public SummonerConfiguration updateMaxSummonMonsterLevel(@Min(0) int maxSummonMonsterLevel) {
        SummonerConfiguration configuration = getConfiguration();
        configuration.setMaxSummonMonsterLevel(maxSummonMonsterLevel);
        return repository.save(configuration);
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
}
