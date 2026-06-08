package com.pathfinder.summons.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.pathfinder.summons.domain.model.CombatState;
import com.pathfinder.summons.domain.model.ConfigurationSummary;
import com.pathfinder.summons.domain.model.DailyUses;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:combat-state-adapter-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class JpaCombatStateRepositoryAdapterIT {

    @Autowired
    private JpaCombatStateRepositoryAdapter adapter;

    @Autowired
    private CombatStateJpaRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        adapter.clear();
    }

    @Test
    void roundTripsCombatStateThroughTheSpringConfiguredObjectMapper() {
        CombatState state = CombatState.builder()
                .activeGroups(List.of())
                .dailyUses(new DailyUses(6, 4))
                .configuration(ConfigurationSummary.builder()
                        .maxSummonMonsterLevel(3)
                        .availableTemplates(List.of())
                        .build())
                .lastRollResult(null)
                .recentlyUsedSummons(List.of())
                .mostUsedSummons(List.of())
                .build();

        adapter.saveCombatState(state);

        assertThat(adapter.getCombatState()).isEqualTo(state);
    }

    @Test
    void removesUnreadableCombatStatePayloadAndReturnsNull() {
        jdbcTemplate.update(
                "insert into combat_state (id, payload) values (?, ?)",
                1L,
                "not-json"
        );

        assertThat(adapter.getCombatState()).isNull();
        assertThat(repository.count()).isZero();
    }
}
