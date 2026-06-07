package com.pathfinder.summons;

import com.pathfinder.summons.application.SummonerConfigurationService;
import com.pathfinder.summons.web.SummonerConfigurationController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KazkumKoldugumSummonsApplicationTests {

    @LocalServerPort
    int port;

    @Autowired
    SummonerConfigurationService service;

    @BeforeEach
    void resetConfiguration() {
        service.updateMaxSummonMonsterLevel(3);
    }

    @Test
    void contextLoads() {
        assertThat(service.getConfiguration().getMaxSummonMonsterLevel()).isEqualTo(3);
    }

    @Test
    void configurationDefaultsToThreeAndCanBeUpdatedViaApi() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        ResponseEntity<SummonerConfigurationController.SummonerConfigurationResponse> initial =
                restClient.get()
                        .uri("/api/configuration")
                        .retrieve()
                        .toEntity(SummonerConfigurationController.SummonerConfigurationResponse.class);

        assertThat(initial.getBody()).isNotNull();
        assertThat(initial.getBody().maxSummonMonsterLevel()).isEqualTo(3);

        SummonerConfigurationController.UpdateSummonerConfigurationRequest request =
                new SummonerConfigurationController.UpdateSummonerConfigurationRequest(5);

        ResponseEntity<SummonerConfigurationController.SummonerConfigurationResponse> updated =
                restClient.put()
                        .uri("/api/configuration")
                        .body(request)
                        .retrieve()
                        .toEntity(SummonerConfigurationController.SummonerConfigurationResponse.class);

        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().maxSummonMonsterLevel()).isEqualTo(5);
        assertThat(service.getConfiguration().getMaxSummonMonsterLevel()).isEqualTo(5);
    }

    @Test
    void summonQuantityUsesConfiguredLevelDifference() {
        service.updateMaxSummonMonsterLevel(4);

        assertThat(service.isCreatureAvailable(4)).isTrue();
        assertThat(service.calculateQuantityFor(4).formula()).isEqualTo("1");
        assertThat(service.calculateQuantityFor(3).formula()).isEqualTo("1d3");
        assertThat(service.calculateQuantityFor(2).formula()).isEqualTo("1d4+1");
    }
}
