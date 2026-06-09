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
    short: 'Subtipo earth, darkvision y daño ácido',
    requiresChoice: true,
  },
  {
    key: 'fiery',
    label: 'Fiery',
    short: 'Subtipo fire, inmunidad al fuego, vulnerabilidad al frío y daño de fuego',
    requiresChoice: true,
  },
  {
    key: 'celestial',
    label: 'Celestial',
    short: 'Smite evil y resistencias sagradas',
    requiresChoice: true,
  },
  {
    key: 'entropic',
    label: 'Entropic',
    short: 'Smite law y resistencias caóticas',
    requiresChoice: true,
  },
  {
    key: 'resolute',
    label: 'Resolute',
    short: 'Smite chaos y resistencias legales',
    requiresChoice: true,
  },
] as const;

export type SummonTemplateKey = (typeof summonTemplates)[number]['key'];
