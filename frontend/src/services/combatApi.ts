import { deleteJson, getJson, postJson } from '@/services/http';
import type { CreatureCatalogListResponse } from '@/types/catalog';
import type {
  ActiveSummonGroup,
  AmountRequest,
  CombatRollResult,
  CombatState,
  DailyUses,
  GroupAttackRollResponse,
  GroupSavingThrowsRollResponse,
  SummonCreatureRequest,
} from '@/types/combat';

export type DailyUsesState = DailyUses;

export interface ConfigurationResponse {
  maxSummonMonsterLevel: number;
  dailyUses: DailyUsesState;
  availableTemplates: string[];
  enabledFixedRules: string[];
}

export interface CombatStateSnapshot {
  activeGroups: ActiveSummonGroup[];
  dailyUses: DailyUsesState;
  configuration: ConfigurationResponse;
  lastRollResult: CombatState['lastRollResult'];
}

export interface DailyUsesMutationResponse {
  dailyUses: DailyUsesState;
  combatState: CombatStateSnapshot;
}

export function fetchConfiguration(): Promise<ConfigurationResponse> {
  return getJson('/api/configuration');
}

export function fetchCombatState(): Promise<CombatState> {
  return getJson('/api/combat-state');
}

export function summonCreature(request: SummonCreatureRequest): Promise<CombatState> {
  return postJson<SummonCreatureRequest, CombatState>('/api/combat-state/summons', request);
}

export function clearCombatState(): Promise<CombatState> {
  return deleteJson<CombatState>('/api/combat-state/summons');
}

export function damageInstance(instanceId: string, amount: number): Promise<CombatState> {
  return postJson<AmountRequest, CombatState>(`/api/combat-state/instances/${encodeURIComponent(instanceId)}/damage`, {
    amount,
  });
}

export function healInstance(instanceId: string, amount: number): Promise<CombatState> {
  return postJson<AmountRequest, CombatState>(`/api/combat-state/instances/${encodeURIComponent(instanceId)}/heal`, {
    amount,
  });
}

export function removeInstance(instanceId: string): Promise<CombatState> {
  return deleteJson<CombatState>(`/api/combat-state/instances/${encodeURIComponent(instanceId)}`);
}

export function clearLastRollResult(): Promise<CombatState> {
  return deleteJson<CombatState>('/api/combat-state/last-roll-result');
}

export function rollGroupAttacks(groupId: string): Promise<GroupAttackRollResponse> {
  return postJson<Record<string, never>, GroupAttackRollResponse>(`/api/combat-state/groups/${encodeURIComponent(groupId)}/roll-attacks`, {});
}

export function rollGroupSavingThrows(groupId: string): Promise<GroupSavingThrowsRollResponse> {
  return postJson<Record<string, never>, GroupSavingThrowsRollResponse>(`/api/combat-state/groups/${encodeURIComponent(groupId)}/roll-saving-throws`, {});
}

export function increaseDailyUses(amount: number): Promise<DailyUsesMutationResponse> {
  return postJson('/api/daily-uses/increase', { amount });
}

export function decreaseDailyUses(amount: number): Promise<DailyUsesMutationResponse> {
  return postJson('/api/daily-uses/decrease', { amount });
}

export function resetDailyUses(): Promise<DailyUsesMutationResponse> {
  return postJson('/api/daily-uses/reset', {});
}

export function fetchCatalogCreatures(): Promise<CreatureCatalogListResponse> {
  return getJson('/api/catalog/creatures');
}

export function getGroupById(groups: ActiveSummonGroup[], groupId: string): ActiveSummonGroup | null {
  return groups.find(group => group.id === groupId) ?? null;
}

export type { CombatRollResult };
