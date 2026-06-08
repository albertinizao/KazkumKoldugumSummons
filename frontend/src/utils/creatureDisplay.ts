const subtypeLabels: Record<string, string> = {
  earth: 'tierra',
  fire: 'fuego',
};

export function formatCreatureTypeWithSubtypes(creatureType: string, subtypes: string[] = []): string {
  if (!subtypes.length) {
    return creatureType;
  }

  const suffix = subtypes
    .map(subtype => subtypeLabels[subtype.toLowerCase()] ?? subtype)
    .join(', ');

  return `${creatureType} (${suffix})`;
}
