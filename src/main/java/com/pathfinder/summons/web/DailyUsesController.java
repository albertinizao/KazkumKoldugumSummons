package com.pathfinder.summons.web;

import com.pathfinder.summons.application.SummonerConfigurationService;
import com.pathfinder.summons.domain.model.CombatState;
import com.pathfinder.summons.domain.model.ConfigurationSummary;
import com.pathfinder.summons.domain.model.DailyUses;
import com.pathfinder.summons.domain.model.FixedRuleType;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-uses")
public class DailyUsesController {

    private final SummonerConfigurationService service;

    public DailyUsesController(SummonerConfigurationService service) {
        this.service = service;
    }

    @PostMapping("/increase")
    public DailyUsesMutationResponse increase(@Valid @RequestBody AmountRequest request) {
        DailyUses dailyUses = service.increaseDailyUses(request.amount());
        return new DailyUsesMutationResponse(dailyUses, snapshotCombatState(dailyUses));
    }

    @PostMapping("/decrease")
    public DailyUsesMutationResponse decrease(@Valid @RequestBody AmountRequest request) {
        DailyUses dailyUses = service.decreaseDailyUses(request.amount());
        return new DailyUsesMutationResponse(dailyUses, snapshotCombatState(dailyUses));
    }

    @PostMapping("/reset")
    public DailyUsesMutationResponse reset() {
        DailyUses dailyUses = service.resetDailyUses();
        return new DailyUsesMutationResponse(dailyUses, snapshotCombatState(dailyUses));
    }

    private CombatStateSnapshot snapshotCombatState(DailyUses dailyUses) {
        CombatState combatState = service.snapshotCombatState();
        ConfigurationSummary configurationSummary = service.getConfigurationSummary();
        return new CombatStateSnapshot(
                combatState.getActiveGroups(),
                dailyUses,
                new ConfigurationSnapshot(
                        configurationSummary.getMaxSummonMonsterLevel(),
                        dailyUses,
                        configurationSummary.getAvailableTemplates(),
                        List.of(
                                FixedRuleType.AUGMENT_SUMMONING,
                                FixedRuleType.SUPERIOR_SUMMONING,
                                FixedRuleType.VERSATILE_SUMMON_MONSTER,
                                FixedRuleType.DEEP_GUARDIAN
                        )
                ),
                combatState.getLastRollResult()
        );
    }

    public record AmountRequest(@NotNull @Min(1) Integer amount) {
    }

    public record DailyUsesMutationResponse(DailyUses dailyUses, CombatStateSnapshot combatState) {
    }

    public record CombatStateSnapshot(List<?> groups, DailyUses dailyUses, ConfigurationSnapshot configuration, Object lastRollResult) {
    }

    public record ConfigurationSnapshot(int maxSummonMonsterLevel,
                                        DailyUses dailyUses,
                                        List<SummonTemplateType> availableTemplates,
                                        List<FixedRuleType> enabledFixedRules) {
    }
}
