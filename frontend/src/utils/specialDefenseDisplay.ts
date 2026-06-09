import type { SpecialDefense } from '@/types/catalog';

const labelMap: Record<SpecialDefense['type'], string> = {
  DAMAGE_REDUCTION: 'RD',
  RESISTANCE: 'Resistencia',
  IMMUNITY: 'Inmunidad',
  SPELL_RESISTANCE: 'SR',
  VULNERABILITY: 'Vulnerabilidad',
  OTHER: 'Otros',
};

export function formatSpecialDefense(defense: SpecialDefense): string {
  const value = defense.value ? ` ${defense.value}` : '';
  if (defense.type === 'OTHER') {
    return `${defense.value ?? 'Otros'}`;
  }

  return `${labelMap[defense.type] ?? defense.type}${value}`;
}

function isImmuneLikeOtherDefense(defense: SpecialDefense): boolean {
  if (defense.type !== 'OTHER' || !defense.value) {
    return false;
  }

  const normalized = defense.value.trim().toLowerCase();
  return normalized.startsWith('immune') || normalized.startsWith('immunity');
}

export function formatSpecialDefenseList(
  defenses: SpecialDefense[],
  types?: SpecialDefense['type'][],
  includeImmuneLikeOtherDefenses = false,
): string {
  const filtered = typeof types === 'undefined'
    ? defenses
    : defenses.filter(defense => types.includes(defense.type));
  const visible = includeImmuneLikeOtherDefenses
    ? [...filtered, ...defenses.filter(isImmuneLikeOtherDefense)]
    : filtered;

  return visible.length > 0 ? visible.map(formatSpecialDefense).join(' · ') : '—';
}
