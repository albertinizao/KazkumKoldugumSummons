export const summonTemplates = [
  {
    key: 'none',
    label: 'Sin plantilla',
    short: 'Invocación base',
    requiresChoice: false,
  },
  {
    key: 'chthonic',
    label: 'Chthonic',
    short: 'Tierra, oscuridad y presión física',
    requiresChoice: true,
  },
  {
    key: 'fiery',
    label: 'Fiery',
    short: 'Daño de fuego añadido',
    requiresChoice: true,
  },
  {
    key: 'celestial',
    label: 'Celestial',
    short: 'Sesgo sagrado y apoyo ofensivo',
    requiresChoice: true,
  },
  {
    key: 'entropic',
    label: 'Entropic',
    short: 'Sesgo caótico y presión ofensiva',
    requiresChoice: true,
  },
  {
    key: 'resolute',
    label: 'Resolute',
    short: 'Sesgo estable y ofensiva resistente',
    requiresChoice: true,
  },
] as const;

export type SummonTemplateKey = (typeof summonTemplates)[number]['key'];
