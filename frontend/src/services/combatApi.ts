import { getJson, postJson } from '@/services/http';

export interface DailyUsesResponse {
  maximum: number;
  remaining: number;
}

export interface LastRollResultResponse {
  text: string;
}

export function fetchCombatState(): Promise<unknown> {
  return getJson('/api/combat-state');
}

export function fetchCatalogCreatures(): Promise<unknown> {
  return getJson('/api/catalog/creatures');
}

export function fetchConfiguration(): Promise<unknown> {
  return getJson('/api/configuration');
}

export function fetchLastRollResult(): Promise<LastRollResultResponse> {
  return getJson('/api/combat-state/last-roll-result');
}

export function increaseDailyUses(): Promise<DailyUsesResponse> {
  return postJson('/api/daily-uses/increase');
}

export function decreaseDailyUses(): Promise<DailyUsesResponse> {
  return postJson('/api/daily-uses/decrease');
}

export function resetDailyUses(): Promise<DailyUsesResponse> {
  return postJson('/api/daily-uses/reset');
}
