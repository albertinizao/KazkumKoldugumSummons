package com.pathfinder.summons.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DailyUsesTest {

    @Test
    void normalizesInvalidPersistedStateIntoValidBounds() {
        DailyUses dailyUses = DailyUses.normalize(-2, 99);

        assertThat(dailyUses.getMaximum()).isEqualTo(0);
        assertThat(dailyUses.getRemaining()).isEqualTo(0);
    }

    @Test
    void increasesButNeverExceedsMaximum() {
        DailyUses dailyUses = new DailyUses(6, 4);

        assertThat(dailyUses.increase(1)).isEqualTo(new DailyUses(6, 5));
        assertThat(dailyUses.increase(99)).isEqualTo(new DailyUses(6, 6));
    }

    @Test
    void decreasesButNeverDropsBelowZero() {
        DailyUses dailyUses = new DailyUses(6, 4);

        assertThat(dailyUses.decrease(1)).isEqualTo(new DailyUses(6, 3));
        assertThat(dailyUses.decrease(99)).isEqualTo(new DailyUses(6, 0));
    }

    @Test
    void rejectsZeroOrNegativeAmountsForManualMutations() {
        DailyUses dailyUses = new DailyUses(6, 4);

        assertThatThrownBy(() -> dailyUses.increase(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
        assertThatThrownBy(() -> dailyUses.decrease(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void consumeOneStopsAtZeroWithoutBlocking() {
        DailyUses dailyUses = new DailyUses(6, 0);

        assertThat(dailyUses.consumeOne()).isSameAs(dailyUses);
    }
}
