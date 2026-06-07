package com.pathfinder.summons.web;

import com.pathfinder.summons.application.CombatStateService;
import com.pathfinder.summons.application.SummonerConfigurationService;
import com.pathfinder.summons.domain.model.ActiveSummonStatus;
import com.pathfinder.summons.domain.model.CombatState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CombatStateControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    CombatStateService combatStateService;

    @Autowired
    SummonerConfigurationService configurationService;

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @BeforeEach
    void setUp() {
        configurationService.updateMaxSummonMonsterLevel(1);
        combatStateService.clearCombatState();
    }

    @Test
    void returnsDefaultCombatState() {
        ResponseEntity<CombatState> response = restClient().get()
                .uri("/api/combat-state")
                .retrieve()
                .toEntity(CombatState.class);

        CombatState state = response.getBody();
        assertThat(state).isNotNull();
        assertThat(state.getActiveGroups()).isEmpty();
        assertThat(state.getDailyUses().getMaximum()).isEqualTo(6);
        assertThat(state.getDailyUses().getRemaining()).isEqualTo(4);
        assertThat(state.getConfiguration().getMaxSummonMonsterLevel()).isEqualTo(1);
        assertThat(state.getLastRollResult()).isNull();
    }

    @Test
    void summonsGroupsInstancesAndSupportsHpMutationDeleteAndClear() {
        CombatState afterFirstSummon = summonBadger();
        assertThat(afterFirstSummon.getActiveGroups()).hasSize(1);
        assertThat(afterFirstSummon.getLastRollResult()).isNotNull();

        var group = afterFirstSummon.getActiveGroups().getFirst();
        var instance = group.getInstances().getFirst();
        int initialHitPoints = instance.getCurrentHitPoints();
        int maxHitPoints = instance.getMaxHitPoints();

        CombatState afterSecondSummon = summonBadger();
        assertThat(afterSecondSummon.getActiveGroups()).hasSize(1);
        assertThat(afterSecondSummon.getActiveGroups().getFirst().getInstances()).hasSize(2);

        CombatState afterDamage = restClient().post()
                .uri("/api/combat-state/instances/{instanceId}/damage", instance.getId())
                .body(new CombatStateController.AmountRequest(1))
                .retrieve()
                .toEntity(CombatState.class)
                .getBody();

        assertThat(afterDamage).isNotNull();
        var damagedInstance = afterDamage.getActiveGroups().getFirst().getInstances().stream()
                .filter(active -> active.getId().equals(instance.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(damagedInstance.getCurrentHitPoints()).isEqualTo(initialHitPoints - 1);
        assertThat(damagedInstance.getStatus())
                .isEqualTo(damagedInstance.getCurrentHitPoints() <= 0 ? ActiveSummonStatus.DOWN : ActiveSummonStatus.DAMAGED);

        CombatState afterHeal = restClient().post()
                .uri("/api/combat-state/instances/{instanceId}/heal", instance.getId())
                .body(new CombatStateController.AmountRequest(1))
                .retrieve()
                .toEntity(CombatState.class)
                .getBody();

        assertThat(afterHeal).isNotNull();
        var healedInstance = afterHeal.getActiveGroups().getFirst().getInstances().stream()
                .filter(active -> active.getId().equals(instance.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(healedInstance.getCurrentHitPoints()).isEqualTo(maxHitPoints);
        assertThat(healedInstance.getStatus()).isEqualTo(ActiveSummonStatus.HEALTHY);

        CombatState afterRemove = restClient().delete()
                .uri("/api/combat-state/instances/{instanceId}", instance.getId())
                .retrieve()
                .toEntity(CombatState.class)
                .getBody();

        assertThat(afterRemove).isNotNull();
        assertThat(afterRemove.getActiveGroups()).hasSize(1);
        assertThat(afterRemove.getActiveGroups().getFirst().getInstances()).hasSize(1);

        CombatState afterClearRoll = restClient().delete()
                .uri("/api/combat-state/last-roll-result")
                .retrieve()
                .toEntity(CombatState.class)
                .getBody();

        assertThat(afterClearRoll).isNotNull();
        assertThat(afterClearRoll.getLastRollResult()).isNull();

        CombatState afterClear = restClient().delete()
                .uri("/api/combat-state/summons")
                .retrieve()
                .toEntity(CombatState.class)
                .getBody();

        assertThat(afterClear).isNotNull();
        assertThat(afterClear.getActiveGroups()).isEmpty();
        assertThat(afterClear.getDailyUses().getMaximum()).isEqualTo(6);
        assertThat(afterClear.getDailyUses().getRemaining()).isEqualTo(4);
    }

    private CombatState summonBadger() {
        ResponseEntity<CombatState> response = restClient().post()
                .uri("/api/combat-state/summons")
                .body(new CombatStateController.SummonCreatureRequest("badger", null, null, "MANUAL_SEARCH"))
                .retrieve()
                .toEntity(CombatState.class);

        CombatState state = response.getBody();
        assertThat(state).isNotNull();
        return state;
    }
}
