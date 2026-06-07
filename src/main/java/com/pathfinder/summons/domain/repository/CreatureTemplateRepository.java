package com.pathfinder.summons.domain.repository;

import com.pathfinder.summons.domain.model.CreatureTemplate;
import java.util.List;
import java.util.Optional;

public interface CreatureTemplateRepository {
    List<CreatureTemplate> findAll();
    Optional<CreatureTemplate> findById(String id);
    List<CreatureTemplate> findBySummonLevelLessThanEqual(int level);
}
