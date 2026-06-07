export const summonTemplates = [
  {
    key: 'none',
    label: 'Sin plantilla',
    short: 'Invocación base',
    requiresChoice: false
  },
  {
    key: 'chthonic',
    label: 'Chthonic',
    short: 'Tierra, oscuridad y ataque ácido',
    requiresChoice: true
  },
  {
    key: 'fiery',
    label: 'Fiery',
    short: 'Fuego, resistencia ígnea y daño de fuego',
    requiresChoice: true
  },
  {
    key: 'celestial',
    label: 'Celestial',
    short: 'Sanctified summon with smite evil',
    requiresChoice: true
  },
  {
    key: 'entropic',
    label: 'Entropic',
    short: 'Chaos-aligned summon with smite good',
    requiresChoice: true
  },
  {
    key: 'resolute',
    label: 'Resolute',
    short: 'Steadfast summon with smite chaos',
    requiresChoice: true
  }
]

export const creatureCatalog = [
  {
    id: 'wolf',
    name: 'Wolf',
    summonLevel: 1,
    size: 'Medium',
    type: 'Animal',
    alignment: 'N',
    source: 'Core',
    allowedTemplates: ['chthonic', 'fiery', 'celestial', 'entropic', 'resolute'],
    speeds: [{ type: 'land', value: 50 }],
    attacks: [{ name: 'Bite', bonus: 3, damage: '1d6+1', type: 'piercing', critical: 'x2' }],
    defenses: { ac: 14, hp: 13, fort: 4, ref: 5, will: 1 },
    abilityScores: { str: 13, dex: 15, con: 15, int: 2, wis: 12, cha: 6 },
    tags: ['Trip', 'Low level'],
    notes: 'Classic front-line summon for early levels.'
  },
  {
    id: 'giant_frog',
    name: 'Giant Frog',
    summonLevel: 1,
    size: 'Small',
    type: 'Animal',
    alignment: 'N',
    source: 'Core',
    allowedTemplates: ['chthonic', 'fiery', 'celestial', 'entropic', 'resolute'],
    speeds: [{ type: 'land', value: 20 }, { type: 'swim', value: 30 }],
    attacks: [{ name: 'Bite', bonus: 4, damage: '1d3+1', type: 'piercing', critical: 'x2' }],
    defenses: { ac: 13, hp: 9, fort: 3, ref: 6, will: 1 },
    abilityScores: { str: 12, dex: 16, con: 13, int: 1, wis: 10, cha: 6 },
    tags: ['Swim', 'Reach'],
    notes: 'Good for keeping a threat in difficult terrain.'
  },
  {
    id: 'dire_bat',
    name: 'Dire Bat',
    summonLevel: 2,
    size: 'Large',
    type: 'Animal',
    alignment: 'N',
    source: 'Core',
    allowedTemplates: ['chthonic', 'fiery', 'celestial', 'entropic', 'resolute'],
    speeds: [{ type: 'land', value: 20 }, { type: 'fly', value: 40 }],
    attacks: [{ name: 'Bite', bonus: 5, damage: '1d8+2', type: 'piercing', critical: 'x2' }],
    defenses: { ac: 14, hp: 22, fort: 5, ref: 8, will: 3 },
    abilityScores: { str: 15, dex: 15, con: 15, int: 2, wis: 12, cha: 6 },
    tags: ['Fly', 'Vision'],
    notes: 'Mobile summon for vertical pressure and scouting.'
  },
  {
    id: 'small_earth_elemental',
    name: 'Small Earth Elemental',
    summonLevel: 2,
    size: 'Small',
    type: 'Elemental',
    subtype: 'Earth',
    alignment: 'N',
    source: 'Core',
    allowedTemplates: ['chthonic', 'resolute'],
    speeds: [{ type: 'burrow', value: 20 }, { type: 'land', value: 20 }],
    attacks: [{ name: 'Slam', bonus: 6, damage: '1d6+4', type: 'bludgeoning', critical: 'x2' }],
    defenses: { ac: 16, hp: 19, fort: 6, ref: 3, will: 4 },
    abilityScores: { str: 16, dex: 12, con: 17, int: 4, wis: 11, cha: 11 },
    tags: ['Earth', 'Burrow'],
    notes: 'Useful for testing earth-subtype and burrow-based bonuses.'
  },
  {
    id: 'lantern_archon',
    name: 'Lantern Archon',
    summonLevel: 3,
    size: 'Tiny',
    type: 'Outsider',
    subtype: 'Good, Extraplanar',
    alignment: 'LG',
    source: 'Core',
    allowedTemplates: ['celestial', 'resolute'],
    speeds: [{ type: 'fly', value: 60 }],
    attacks: [{ name: 'Aura of Menace', bonus: 0, damage: '—', type: 'light', critical: '—' }],
    defenses: { ac: 15, hp: 13, fort: 4, ref: 5, will: 4 },
    abilityScores: { str: 1, dex: 13, con: 10, int: 10, wis: 10, cha: 11 },
    tags: ['Utility', 'Radiant'],
    notes: 'Natural celestial fit with built-in support flavor.'
  },
  {
    id: 'fiendish_ape',
    name: 'Fiendish Ape',
    summonLevel: 4,
    size: 'Large',
    type: 'Animal',
    alignment: 'N',
    source: 'Core',
    allowedTemplates: ['entropic', 'fiery', 'chthonic'],
    speeds: [{ type: 'land', value: 40 }, { type: 'climb', value: 30 }],
    attacks: [{ name: 'Bite', bonus: 8, damage: '1d6+4', type: 'piercing', critical: 'x2' }, { name: 'Claw', bonus: 8, damage: '1d8+4', type: 'slashing', critical: 'x2' }],
    defenses: { ac: 17, hp: 37, fort: 8, ref: 7, will: 4 },
    abilityScores: { str: 18, dex: 15, con: 16, int: 2, wis: 12, cha: 7 },
    tags: ['Climb', 'High damage'],
    notes: 'Strong mid-level bruiser for a more aggressive front line.'
  }
]
