package com.pathfinder.summons.domain.model;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder
public class RollDisplay {
    String id;
    RollDisplayType type;
    String title;
    LocalDateTime createdAt;
    String content;
}
