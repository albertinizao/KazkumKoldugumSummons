package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class SummonShortcut {
    String id;
    String creatureTemplateId;
    SummonTemplateType selectedTemplate; // Nullable
    String displayName;
    int usageCount;
    LocalDateTime lastUsedAt;
}
