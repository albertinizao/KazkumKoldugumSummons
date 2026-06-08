package com.pathfinder.summons.web;

import java.util.Map;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DailyUsesControllerIT {

    @LocalServerPort
    int port;

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void exposesDailyUsesInConfiguration() {
        ResponseEntity<SummonerConfigurationController.SummonerConfigurationResponse> response = restClient().get()
                .uri("/api/configuration")
                .retrieve()
                .toEntity(SummonerConfigurationController.SummonerConfigurationResponse.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().dailyUses().getMaximum()).isEqualTo(6);
        assertThat(response.getBody().dailyUses().getRemaining()).isEqualTo(4);
    }

    @Test
    void increasesDecreasesAndResetsDailyUsesWithinBounds() {
        ResponseEntity<DailyUsesController.DailyUsesMutationResponse> increased = restClient().post()
                .uri("/api/daily-uses/increase")
                .body(Map.of("amount", 1))
                .retrieve()
                .toEntity(DailyUsesController.DailyUsesMutationResponse.class);

        assertThat(increased.getBody()).isNotNull();
        assertThat(increased.getBody().dailyUses().getRemaining()).isEqualTo(5);

        ResponseEntity<DailyUsesController.DailyUsesMutationResponse> decreased = restClient().post()
                .uri("/api/daily-uses/decrease")
                .body(Map.of("amount", 2))
                .retrieve()
                .toEntity(DailyUsesController.DailyUsesMutationResponse.class);

        assertThat(decreased.getBody()).isNotNull();
        assertThat(decreased.getBody().dailyUses().getRemaining()).isEqualTo(3);

        ResponseEntity<DailyUsesController.DailyUsesMutationResponse> reset = restClient().post()
                .uri("/api/daily-uses/reset")
                .body(Map.of())
                .retrieve()
                .toEntity(DailyUsesController.DailyUsesMutationResponse.class);

        assertThat(reset.getBody()).isNotNull();
        assertThat(reset.getBody().dailyUses().getRemaining()).isEqualTo(6);
        assertThat(reset.getBody().combatState().configuration().dailyUses().getRemaining()).isEqualTo(6);
    }

    @Test
    void rejectsInvalidAmounts() {
        String body = restClient().post()
                .uri("/api/daily-uses/increase")
                .body(Map.of("amount", 0))
                .exchange((request, response) -> new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));

        assertThat(body).contains("INVALID_DAILY_USES");
    }
}
