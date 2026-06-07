package com.pathfinder.summons.web;

import com.pathfinder.summons.application.SummonQuantityCalculator;
import com.pathfinder.summons.application.SummonerConfigurationService;
import com.pathfinder.summons.domain.model.SummonerConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
        return SummonerConfigurationResponse.from(service.updateMaxSummonMonsterLevel(request.maxSummonMonsterLevel()));
    }

    public record UpdateSummonerConfigurationRequest(@NotNull @Min(0) Integer maxSummonMonsterLevel) {
    }

    public record SummonerConfigurationResponse(int maxSummonMonsterLevel) {
        static SummonerConfigurationResponse from(SummonerConfiguration configuration) {
            return new SummonerConfigurationResponse(configuration.getMaxSummonMonsterLevel());
        }
    }
}
