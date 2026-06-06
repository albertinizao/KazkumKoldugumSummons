package com.pathfinder.summons.domain.service;

import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.ResolvedCreature;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import com.pathfinder.summons.domain.model.SummonerConfiguration;

public interface CreatureResolver {
    ResolvedCreature resolve(CreatureTemplate template, SummonTemplateType templateType, SummonerConfiguration config);
}
