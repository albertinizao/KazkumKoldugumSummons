package com.pathfinder.summons.web;

import com.pathfinder.summons.application.SummonQuantityCalculator;
import com.pathfinder.summons.application.SummonerConfigurationService;
import com.pathfinder.summons.domain.model.DailyUses;
import com.pathfinder.summons.domain.model.FixedRuleType;
import com.pathfinder.summons.domain.model.SummonerConfiguration;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/configuration", "/api/summoner-configuration"})
public class SummonerConfigurationController {

    private final SummonerConfigurationService service;

    public SummonerConfigurationController(SummonerConfigurationService service) {
        this.service = service;
    }

    @GetMapping
    public SummonerConfigurationResponse getConfiguration() {
        return SummonerConfigurationResponse.from(service.getConfiguration());
    }

    @PutMapping
    public SummonerConfigurationResponse updateConfiguration(@Valid @RequestBody UpdateSummonerConfigurationRequest request) {
        return SummonerConfigurationResponse.from(service.updateConfiguration(
                request.maxSummonMonsterLevel(),
                request.dailyUsesMaximum()
        ));
    }

    public record UpdateSummonerConfigurationRequest(@NotNull @Min(0) Integer maxSummonMonsterLevel,
                                                     @NotNull @Min(0) Integer dailyUsesMaximum) {
    }

    public record SummonerConfigurationResponse(int maxSummonMonsterLevel,
                                                DailyUses dailyUses,
                                                List<SummonTemplateType> availableTemplates,
                                                List<FixedRuleType> enabledFixedRules) {
        static SummonerConfigurationResponse from(SummonerConfiguration configuration) {
            return new SummonerConfigurationResponse(
                    configuration.getMaxSummonMonsterLevel(),
                    configuration.getDailyUses(),
                    List.of(SummonTemplateType.values()),
                    List.of(
                            FixedRuleType.AUGMENT_SUMMONING,
                            FixedRuleType.SUPERIOR_SUMMONING,
                            FixedRuleType.VERSATILE_SUMMON_MONSTER,
                            FixedRuleType.DEEP_GUARDIAN
                    )
            );
        }
    }
}
