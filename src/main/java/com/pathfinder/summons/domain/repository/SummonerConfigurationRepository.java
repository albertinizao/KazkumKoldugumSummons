package com.pathfinder.summons.domain.repository;

import com.pathfinder.summons.domain.model.SummonerConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummonerConfigurationRepository extends JpaRepository<SummonerConfiguration, Long> {
}
