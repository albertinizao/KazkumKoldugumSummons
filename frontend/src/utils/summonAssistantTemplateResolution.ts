import { summonTemplates } from '@/data/summonTemplates';
import type { SummonTemplateType } from '@/types/catalog';

const DEFAULT_TEMPLATE_PRIORITY: SummonTemplateType[] = [
  'CHTHONIC',
  'RESOLUTE',
  'CELESTIAL',
  'ENTROPIC',
  'FIERY',
];

export function getAllowedSummonTemplatesForCreatureType(creatureType: string | null | undefined): SummonTemplateType[] {
  const normalizedCreatureType = creatureType?.toLowerCase() ?? '';
  if (normalizedCreatureType.includes('outsider')) {
    return [];
  }

  return summonTemplates
    .filter(template => template.key !== 'none')
    .map(template => template.key.toUpperCase() as SummonTemplateType);
}

function allows(allowedTemplates: SummonTemplateType[], template: SummonTemplateType): boolean {
  return allowedTemplates.includes(template);
}

function prefersCelestial(choiceKey: string): boolean {
  return choiceKey.includes('FRIO') || choiceKey.includes('ELECTRICIDAD');
}

function prefersResolute(choiceKey: string): boolean {
  return (
    choiceKey.includes('VENENO_ENFERMEDAD')
    || choiceKey.includes('MENTE_MIEDO')
    || choiceKey.includes('DAÑO_FISICO')
    || choiceKey.includes('AREAS')
    || choiceKey.includes('ELEMENTAL_GENERAL')
  );
}

export function resolveSummonAssistantTemplate(
  choiceKey: string,
  creatureType: string | null | undefined,
  explicitTemplate: SummonTemplateType | null,
): SummonTemplateType | null {
  const allowedTemplates = getAllowedSummonTemplatesForCreatureType(creatureType);

  if (allowedTemplates.length === 0) {
    return null;
  }

  if (prefersCelestial(choiceKey) && allows(allowedTemplates, 'CELESTIAL')) {
    return 'CELESTIAL';
  }

  if (prefersResolute(choiceKey) && allows(allowedTemplates, 'RESOLUTE')) {
    return 'RESOLUTE';
  }

  if (explicitTemplate && allows(allowedTemplates, explicitTemplate)) {
    return explicitTemplate;
  }

  for (const template of DEFAULT_TEMPLATE_PRIORITY) {
    if (allows(allowedTemplates, template)) {
      return template;
    }
  }

  return allowedTemplates[0] ?? null;
}
