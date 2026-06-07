package com.pathfinder.summons.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "summoner_configuration")
public class SummonerConfiguration {

    public static final long SINGLETON_ID = 1L;
    public static final int DEFAULT_MAX_SUMMON_MONSTER_LEVEL = 3;

    @Id
    private Long id;

    @Column(nullable = false)
    private int maxSummonMonsterLevel = DEFAULT_MAX_SUMMON_MONSTER_LEVEL;

    protected SummonerConfiguration() {
        // JPA only
    }

    private SummonerConfiguration(Long id, int maxSummonMonsterLevel) {
        this.id = id;
        this.maxSummonMonsterLevel = maxSummonMonsterLevel;
    }

    public static SummonerConfiguration defaultConfiguration() {
        return new SummonerConfiguration(SINGLETON_ID, DEFAULT_MAX_SUMMON_MONSTER_LEVEL);
    }

    public Long getId() {
        return id;
    }

    public int getMaxSummonMonsterLevel() {
        return maxSummonMonsterLevel;
    }

    public void setMaxSummonMonsterLevel(int maxSummonMonsterLevel) {
        this.maxSummonMonsterLevel = maxSummonMonsterLevel;
    }
}
