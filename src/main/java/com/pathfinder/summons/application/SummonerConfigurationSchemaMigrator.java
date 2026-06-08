package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.DailyUses;
import com.pathfinder.summons.domain.model.SummonerConfiguration;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SummonerConfigurationSchemaMigrator {

    private static final String TABLE_NAME = "summoner_configuration";
    private static final String MAX_SUMMON_MONSTER_LEVEL = "max_summon_monster_level";
    private static final String DAILY_USES_MAXIMUM = "daily_uses_maximum";
    private static final String DAILY_USES_REMAINING = "daily_uses_remaining";

    private final JdbcTemplate jdbcTemplate;

    public SummonerConfigurationSchemaMigrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void migrate() {
        if (!tableExists()) {
            return;
        }

        ensureColumn(DAILY_USES_MAXIMUM, "INT DEFAULT " + DailyUses.DEFAULT_MAXIMUM + " NOT NULL");
        ensureColumn(DAILY_USES_REMAINING, "INT DEFAULT " + DailyUses.DEFAULT_REMAINING + " NOT NULL");

        jdbcTemplate.update(
                "UPDATE " + TABLE_NAME + " SET " + DAILY_USES_MAXIMUM + " = ? " +
                        "WHERE " + DAILY_USES_MAXIMUM + " IS NULL",
                DailyUses.DEFAULT_MAXIMUM
        );
        jdbcTemplate.update(
                "UPDATE " + TABLE_NAME + " SET " + DAILY_USES_REMAINING + " = ? " +
                        "WHERE " + DAILY_USES_REMAINING + " IS NULL",
                DailyUses.DEFAULT_REMAINING
        );

        if (!hasRow(SummonerConfiguration.SINGLETON_ID)) {
            jdbcTemplate.update(
                    "INSERT INTO " + TABLE_NAME + " (id, " + MAX_SUMMON_MONSTER_LEVEL + ", " +
                            DAILY_USES_MAXIMUM + ", " + DAILY_USES_REMAINING + ") VALUES (?, ?, ?, ?)",
                    SummonerConfiguration.SINGLETON_ID,
                    SummonerConfiguration.DEFAULT_MAX_SUMMON_MONSTER_LEVEL,
                    DailyUses.DEFAULT_MAXIMUM,
                    DailyUses.DEFAULT_REMAINING
            );
        }
    }

    private boolean tableExists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE lower(table_name) = ?",
                Integer.class,
                TABLE_NAME.toLowerCase(Locale.ROOT)
        );
        return count != null && count > 0;
    }

    private boolean hasRow(long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE id = ?",
                Integer.class,
                id
        );
        return count != null && count > 0;
    }

    private void ensureColumn(String columnName, String definition) {
        if (columnExists(columnName)) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " " + definition);
    }

    private boolean columnExists(String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE lower(table_name) = ? AND lower(column_name) = ?",
                Integer.class,
                TABLE_NAME.toLowerCase(Locale.ROOT),
                columnName.toLowerCase(Locale.ROOT)
        );
        return count != null && count > 0;
    }
}
