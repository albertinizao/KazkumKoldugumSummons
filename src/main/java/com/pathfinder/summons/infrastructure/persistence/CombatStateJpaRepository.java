package com.pathfinder.summons.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CombatStateJpaRepository extends JpaRepository<CombatStateEntity, Long> {
}
