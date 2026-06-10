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

export interface DiceRoll {
  formula: string;
  naturalResults: number[];
  modifier: number;
  total: number;
  label?: string | null;
}

export interface DamageComponentRollResult {
  formula: string;
  appliedFormula: string;
  roll: DiceRoll;
  damageType: 'PIERCING' | 'SLASHING' | 'BLUDGEONING' | 'FIRE' | 'COLD' | 'ACID' | 'ELECTRICITY' | 'SONIC' | 'FORCE' | 'UNTYPED' | 'OTHER';
  multipliesOnCritical: boolean;
  total: number;
}

export interface DamageRollResult {
  components: DamageComponentRollResult[];
  total: number;
  displayText: string;
}

export interface CriticalThreatResult {
  confirmationRoll: DiceRoll;
  criticalDamage: DamageRollResult;
}

export interface SingleAttackRollResult {
  attackId: string;
  attackName: string;
  attackIndex: number | null;
  attackRoll: DiceRoll;
  normalDamage: DamageRollResult;
  criticalThreat: CriticalThreatResult | null;
  displayText: string;
}

export interface CreatureAttackRollResult {
  instanceId: string;
  instanceDisplayName: string;
  attackResults: SingleAttackRollResult[];
  displayText: string;
}

export interface GroupAttackRollResult {
  id: string;
  type: 'ATTACK_GROUP';
  title: string;
  groupId: string;
  creatureName: string;
  createdAt: string;
  instanceResults: CreatureAttackRollResult[];
  displayText: string;
}

export interface GroupAttackRollResponse {
  rollResult: GroupAttackRollResult;
  combatState: CombatState;
}

export interface GlobalCombatRollResult {
  title: string;
  displayText: string;
  results: CombatRollResult[];
}

export interface CreatureSavingThrowsRollResult {
  instanceId: string;
  instanceDisplayName: string;
  fortitude: DiceRoll;
  reflex: DiceRoll;
  will: DiceRoll;
  displayText: string;
}

export interface GroupSavingThrowsRollResult {
  id: string;
  type: 'SAVING_THROWS_GROUP';
  title: string;
  groupId: string;
  creatureName: string;
  createdAt: string;
  instanceResults: CreatureSavingThrowsRollResult[];
  displayText: string;
}

export interface GroupSavingThrowsRollResponse {
  rollResult: GroupSavingThrowsRollResult;
  combatState: CombatState;
}

export type CombatRollResult = GroupAttackRollResult | GroupSavingThrowsRollResult;

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
