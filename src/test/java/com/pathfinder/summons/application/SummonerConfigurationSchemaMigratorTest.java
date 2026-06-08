package com.pathfinder.summons.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.pathfinder.summons.domain.model.DailyUses;
import com.pathfinder.summons.domain.model.SummonerConfiguration;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class SummonerConfigurationSchemaMigratorTest {

    @Test
    void migrateAddsMissingDailyUsesColumnsToLegacySchema() {
        DataSource dataSource = createDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                create table summoner_configuration (
                    id bigint not null primary key,
                    max_summon_monster_level integer not null
                )
                """);
        jdbcTemplate.update(
                "insert into summoner_configuration (id, max_summon_monster_level) values (?, ?)",
                SummonerConfiguration.SINGLETON_ID,
                5
        );

        new SummonerConfigurationSchemaMigrator(jdbcTemplate).migrate();

        assertThat(jdbcTemplate.queryForObject(
                "select daily_uses_maximum from summoner_configuration where id = ?",
                Integer.class,
                SummonerConfiguration.SINGLETON_ID
        )).isEqualTo(DailyUses.DEFAULT_MAXIMUM);

        assertThat(jdbcTemplate.queryForObject(
                "select daily_uses_remaining from summoner_configuration where id = ?",
                Integer.class,
                SummonerConfiguration.SINGLETON_ID
        )).isEqualTo(DailyUses.DEFAULT_REMAINING);
    }

    @Test
    void migrateInsertsDefaultConfigurationWhenTableIsEmpty() {
        DataSource dataSource = createDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                create table summoner_configuration (
                    id bigint not null primary key,
                    max_summon_monster_level integer not null
                )
                """);

        new SummonerConfigurationSchemaMigrator(jdbcTemplate).migrate();

        assertThat(jdbcTemplate.queryForObject("select count(*) from summoner_configuration", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select max_summon_monster_level from summoner_configuration where id = ?",
                Integer.class,
                SummonerConfiguration.SINGLETON_ID
        )).isEqualTo(SummonerConfiguration.DEFAULT_MAX_SUMMON_MONSTER_LEVEL);
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:config-migration-" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
