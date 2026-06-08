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

    @Column(nullable = false)
    private int dailyUsesMaximum = DailyUses.DEFAULT_MAXIMUM;

    @Column(nullable = false)
    private int dailyUsesRemaining = DailyUses.DEFAULT_REMAINING;

    protected SummonerConfiguration() {
        // JPA only
    }

    private SummonerConfiguration(Long id, int maxSummonMonsterLevel, int dailyUsesMaximum, int dailyUsesRemaining) {
        this.id = id;
        this.maxSummonMonsterLevel = maxSummonMonsterLevel;
        this.dailyUsesMaximum = dailyUsesMaximum;
        this.dailyUsesRemaining = dailyUsesRemaining;
    }

    public static SummonerConfiguration defaultConfiguration() {
        return new SummonerConfiguration(
                SINGLETON_ID,
                DEFAULT_MAX_SUMMON_MONSTER_LEVEL,
                DailyUses.DEFAULT_MAXIMUM,
                DailyUses.DEFAULT_REMAINING
        );
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

    public int getDailyUsesMaximum() {
        return dailyUsesMaximum;
    }

    public int getDailyUsesRemaining() {
        return dailyUsesRemaining;
    }

    public DailyUses getDailyUses() {
        return DailyUses.normalize(dailyUsesMaximum, dailyUsesRemaining);
    }

    public void setDailyUses(DailyUses dailyUses) {
        this.dailyUsesMaximum = dailyUses.getMaximum();
        this.dailyUsesRemaining = dailyUses.getRemaining();
    }
}
