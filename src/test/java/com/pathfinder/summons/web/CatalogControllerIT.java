package com.pathfinder.summons.web;

import com.pathfinder.summons.application.CreatureCatalogService;
import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.ResolvedCreature;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    CreatureCatalogService catalogService;

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void listsCreaturesAndSupportsSearchAndTemplateFilter() {
        ResponseEntity<CatalogController.CreatureCatalogListResponse> allResponse = restClient().get()
                .uri("/api/catalog/creatures")
                .retrieve()
                .toEntity(CatalogController.CreatureCatalogListResponse.class);

        assertThat(allResponse.getBody()).isNotNull();
        assertThat(allResponse.getBody().total()).isEqualTo(1);
        assertThat(allResponse.getBody().items()).hasSize(1);
        assertThat(allResponse.getBody().items().getFirst().name()).isEqualTo("Badger");

        ResponseEntity<CatalogController.CreatureCatalogListResponse> filteredResponse = restClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/catalog/creatures")
                        .queryParam("query", "badger")
                        .queryParam("template", "FIERY")
                        .build())
                .retrieve()
                .toEntity(CatalogController.CreatureCatalogListResponse.class);

        assertThat(filteredResponse.getBody()).isNotNull();
        assertThat(filteredResponse.getBody().total()).isEqualTo(1);
        assertThat(filteredResponse.getBody().items()).extracting(CatalogController.CreatureCatalogItem::id)
                .containsExactly("badger");
    }

    @Test
    void returnsCreatureDetailAndResolvedPreview() {
        ResponseEntity<CreatureTemplate> detailResponse = restClient().get()
                .uri("/api/catalog/creatures/badger")
                .retrieve()
                .toEntity(CreatureTemplate.class);

        assertThat(detailResponse.getBody()).isNotNull();
        assertThat(detailResponse.getBody().getName()).isEqualTo("Badger");
        assertThat(detailResponse.getBody().getHitPoints().getMaximum()).isEqualTo(6);

        ResponseEntity<ResolvedCreature> previewResponse = restClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/catalog/creatures/badger/resolved-preview")
                        .queryParam("template", "FIERY")
                        .build())
                .retrieve()
                .toEntity(ResolvedCreature.class);

        assertThat(previewResponse.getBody()).isNotNull();
        assertThat(previewResponse.getBody().getBaseTemplateId()).isEqualTo("badger");
        assertThat(previewResponse.getBody().getDisplayName()).isEqualTo("Fiery Badger");
        assertThat(previewResponse.getBody().getAppliedRules())
                .extracting(rule -> rule.getType().name())
                .contains("AUGMENT_SUMMONING", "VERSATILE_SUMMON_MONSTER", "DEEP_GUARDIAN");
        assertThat(previewResponse.getBody().getAttacksText()).contains("fire");
        assertThat(previewResponse.getBody().getSpecialDefenses())
                .extracting(defense -> defense.getType().name() + ":" + defense.getValue())
                .contains("RESISTANCE:fire 10", "IMMUNITY:fire", "VULNERABILITY:cold");
        assertThat(previewResponse.getBody().getFullStatBlock()).contains("Fiery Badger");
    }

    @Test
    void rejectsUnknownCreatures() {
        Integer status = restClient().get()
                .uri("/api/catalog/creatures/unknown")
                .exchange((request, response) -> response.getStatusCode().value());

        assertThat(status).isEqualTo(404);
    }
}
