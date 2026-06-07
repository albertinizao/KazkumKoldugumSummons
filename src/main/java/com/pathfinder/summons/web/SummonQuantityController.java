package com.pathfinder.summons.web;

import com.pathfinder.summons.application.SummonQuantityCalculator;
import com.pathfinder.summons.application.SummonerConfigurationService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summons")
public class SummonQuantityController {

    private final SummonerConfigurationService service;

    public SummonQuantityController(SummonerConfigurationService service) {
        this.service = service;
    }

    @GetMapping("/quantity")
    public QuantityResponse quantity(@RequestParam @Min(0) int creatureSummonLevel) {
        SummonQuantityCalculator.SummonQuantityPlan plan = service.calculateQuantityFor(creatureSummonLevel);
        return new QuantityResponse(service.isCreatureAvailable(creatureSummonLevel), plan.formula(), plan.maximumPossibleQuantity());
    }

    public record QuantityResponse(boolean available, String formula, int maximumPossibleQuantity) {
    }
}
