import type { SummonTemplateType } from '@/types/catalog';
import type { CombatState } from '@/types/combat';

const STORAGE_KEY = 'kazkum-koldugum-summons.combat-state.v1';
const ALLOWED_TEMPLATES: readonly SummonTemplateType[] = [
  'CHTHONIC',
  'FIERY',
  'CELESTIAL',
  'ENTROPIC',
  'RESOLUTE',
];

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function isSummonTemplateType(value: unknown): value is SummonTemplateType {
  return typeof value === 'string' && ALLOWED_TEMPLATES.includes(value as SummonTemplateType);
}

function isSummonTemplateTypeArray(value: unknown): value is SummonTemplateType[] {
  return Array.isArray(value) && value.every(isSummonTemplateType);
}

function normalizeDailyUsesValue(value: unknown, fallback: CombatState['dailyUses']): CombatState['dailyUses'] {
  if (!isRecord(value)) {
    return fallback;
  }

  const maximum = Number(value.maximum);
  const remaining = Number(value.remaining);

  if (!Number.isFinite(maximum) || !Number.isFinite(remaining)) {
    return fallback;
  }

  return {
    maximum: Math.max(0, Math.trunc(maximum)),
    remaining: Math.max(0, Math.trunc(remaining)),
  };
}

function normalizeConfigurationValue(
  value: unknown,
  fallback: CombatState['configuration'],
): CombatState['configuration'] {
  if (!isRecord(value)) {
    return fallback;
  }

  const maxSummonMonsterLevel = Number(value.maxSummonMonsterLevel);

  if (!Number.isFinite(maxSummonMonsterLevel) || !isSummonTemplateTypeArray(value.availableTemplates)) {
    return fallback;
  }

  return {
    maxSummonMonsterLevel: Math.max(0, Math.trunc(maxSummonMonsterLevel)),
    availableTemplates: [...value.availableTemplates],
  };
}

function normalizeCombatState(value: unknown, fallback: CombatState): CombatState {
  if (!isRecord(value)) {
    return fallback;
  }

  return {
    activeGroups: Array.isArray(value.activeGroups) ? value.activeGroups as CombatState['activeGroups'] : fallback.activeGroups,
    dailyUses: normalizeDailyUsesValue(value.dailyUses, fallback.dailyUses),
    configuration: normalizeConfigurationValue(value.configuration, fallback.configuration),
    lastRollResult: value.lastRollResult === null || isRecord(value.lastRollResult)
      ? value.lastRollResult as CombatState['lastRollResult']
      : fallback.lastRollResult,
    recentlyUsedSummons: Array.isArray(value.recentlyUsedSummons)
      ? value.recentlyUsedSummons as CombatState['recentlyUsedSummons']
      : fallback.recentlyUsedSummons,
    mostUsedSummons: Array.isArray(value.mostUsedSummons)
      ? value.mostUsedSummons as CombatState['mostUsedSummons']
      : fallback.mostUsedSummons,
  };
}

export function readCombatStateSnapshot(fallback: CombatState): CombatState | null {
  if (typeof window === 'undefined') {
    return null;
  }

  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }

    return normalizeCombatState(JSON.parse(raw), fallback);
  } catch {
    return null;
  }
}

export function writeCombatStateSnapshot(combatState: CombatState): void {
  if (typeof window === 'undefined') {
    return;
  }

  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(combatState));
  } catch {
    // Best effort only. The API remains the source of truth.
  }
}
