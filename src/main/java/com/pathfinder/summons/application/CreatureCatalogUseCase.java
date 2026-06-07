package com.pathfinder.summons.application;

import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.SummonShortcut;
import java.util.List;

public interface CreatureCatalogUseCase {
    List<CreatureTemplate> getAvailableCreatures();
    List<SummonShortcut> getRecentShortcuts();
    List<SummonShortcut> getMostUsedShortcuts();
    String getFullStatBlock(String groupId);
}
