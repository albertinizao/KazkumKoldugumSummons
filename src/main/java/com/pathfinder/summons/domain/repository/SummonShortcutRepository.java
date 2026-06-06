package com.pathfinder.summons.domain.repository;

import com.pathfinder.summons.domain.model.SummonShortcut;
import java.util.List;
import java.util.Optional;

public interface SummonShortcutRepository {
    List<SummonShortcut> findRecent(int limit);
    List<SummonShortcut> findMostUsed(int limit);
    Optional<SummonShortcut> findByTemplateAndType(String templateId, String templateType);
    void save(SummonShortcut shortcut);
    void clear();
}
