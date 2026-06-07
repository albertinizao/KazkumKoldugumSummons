import { getJson, postJson } from '@/services/http';

export interface DailyUsesState {
  maximum: number;
  remaining: number;
}

export interface ConfigurationResponse {
  maxSummonMonsterLevel: number;
  dailyUses: DailyUsesState;
  availableTemplates: string[];
  enabledFixedRules: string[];
}

export interface CombatStateSnapshot {
  groups: unknown[];
  dailyUses: DailyUsesState;
  configuration: {
    maxSummonMonsterLevel: number;
    dailyUses: DailyUsesState;
    availableTemplates: string[];
    enabledFixedRules: string[];
  };
  lastRollResult: unknown | null;
}

export interface DailyUsesMutationResponse {
  dailyUses: DailyUsesState;
  combatState: CombatStateSnapshot;
}

export function fetchConfiguration(): Promise<ConfigurationResponse> {
  return getJson('/api/configuration');
}

export function fetchCombatState(): Promise<CombatStateSnapshot> {
  return getJson('/api/combat-state');
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
