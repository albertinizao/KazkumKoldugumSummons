import { deleteJson, getJson, postJson } from '@/services/http';
import type {
  ActiveSummonGroup,
  AmountRequest,
  CombatState,
  SummonCreatureRequest,
} from '@/types/combat';
import type { CreatureCatalogListResponse } from '@/types/catalog';

export async function fetchCombatState(): Promise<CombatState> {
  return getJson('/api/combat-state');
}

export async function summonCreature(request: SummonCreatureRequest): Promise<CombatState> {
  return postJson<SummonCreatureRequest, CombatState>('/api/combat-state/summons', request);
}

export async function clearCombatState(): Promise<CombatState> {
  return deleteJson<CombatState>('/api/combat-state/summons');
}

export async function damageInstance(instanceId: string, amount: number): Promise<CombatState> {
  return postJson<AmountRequest, CombatState>(`/api/combat-state/instances/${encodeURIComponent(instanceId)}/damage`, {
    amount,
  });
}

export async function healInstance(instanceId: string, amount: number): Promise<CombatState> {
  return postJson<AmountRequest, CombatState>(`/api/combat-state/instances/${encodeURIComponent(instanceId)}/heal`, {
    amount,
  });
}

export async function removeInstance(instanceId: string): Promise<CombatState> {
  return deleteJson<CombatState>(`/api/combat-state/instances/${encodeURIComponent(instanceId)}`);
}

export async function clearLastRollResult(): Promise<CombatState> {
  return deleteJson<CombatState>('/api/combat-state/last-roll-result');
}

export function fetchCatalogCreatures(): Promise<CreatureCatalogListResponse> {
  return getJson('/api/catalog/creatures');
}

export function getGroupById(groups: ActiveSummonGroup[], groupId: string): ActiveSummonGroup | null {
  return groups.find(group => group.id === groupId) ?? null;
}
