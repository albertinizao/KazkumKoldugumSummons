import type { Alignment, CreatureSize, ResolvedCreature, SummonTemplateType } from '@/types/catalog';

export type CreatureInstanceState = 'HEALTHY' | 'DAMAGED' | 'DOWN';

export interface DailyUses {
  maximum: number;
  remaining: number;
}

export interface ConfigurationSummary {
  maxSummonMonsterLevel: number;
  availableTemplates: SummonTemplateType[];
}

export interface RollDisplay {
  id: string;
  type: 'ATTACK_GROUP' | 'SAVING_THROWS_GROUP' | 'SUMMON_QUANTITY' | 'GENERIC';
  title: string;
  createdAt: string;
  content: string;
}

export interface SummonShortcut {
  id: string;
  creatureTemplateId: string;
  selectedTemplate: SummonTemplateType | null;
  displayName: string;
  usageCount: number;
  lastUsedAt: string;
}

export interface ActiveSummonInstance {
  id: string;
  displayNumber: number;
  displayName: string;
  currentHitPoints: number;
  maxHitPoints: number;
  status: CreatureInstanceState;
}

export interface ActiveSummonGroup {
  id: string;
  resolvedCreature: ResolvedCreature;
  instances: ActiveSummonInstance[];
}

export interface CombatState {
  activeGroups: ActiveSummonGroup[];
  dailyUses: DailyUses;
  configuration: ConfigurationSummary;
  lastRollResult: RollDisplay | null;
  recentlyUsedSummons: SummonShortcut[];
  mostUsedSummons: SummonShortcut[];
}

export interface SummonCreatureRequest {
  creatureTemplateId?: string | null;
  selectedTemplate?: SummonTemplateType | null;
  shortcutId?: string | null;
  source?: string | null;
}

export interface AmountRequest {
  amount: number;
}
