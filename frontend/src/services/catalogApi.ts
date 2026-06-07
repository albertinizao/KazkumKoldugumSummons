import { getJson } from '@/services/http';
import type {
  CreatureCatalogListResponse,
  CreatureTemplate,
  ResolvedCreature,
  SummonTemplateType,
} from '@/types/catalog';

function buildQuery(params: Record<string, string | number | null | undefined>): string {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value === null || value === undefined || value === '') {
      return;
    }

    search.set(key, String(value));
  });

  const query = search.toString();
  return query ? `?${query}` : '';
}

export function fetchCatalogCreatures(params: {
  query?: string;
  summonLevel?: number | null;
  maxSummonLevel?: number | null;
  template?: SummonTemplateType | 'NONE' | null;
  limit?: number;
  offset?: number;
} = {}): Promise<CreatureCatalogListResponse> {
  return getJson(`/api/catalog/creatures${buildQuery(params)}`);
}

export function fetchCreatureTemplate(creatureTemplateId: string): Promise<CreatureTemplate> {
  return getJson(`/api/catalog/creatures/${encodeURIComponent(creatureTemplateId)}`);
}

export function fetchResolvedCreaturePreview(
  creatureTemplateId: string,
  template?: SummonTemplateType | 'NONE' | null,
): Promise<ResolvedCreature> {
  return getJson(
    `/api/catalog/creatures/${encodeURIComponent(creatureTemplateId)}/resolved-preview${buildQuery({ template })}`,
  );
}
