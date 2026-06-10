import type { SummonTemplateType } from '@/types/catalog';

export type SummonAssistantLevel = 3 | 2 | 1;

export type SummonAssistantProfileKey =
  | 'damage_single'
  | 'damage_many'
  | 'tank'
  | 'block'
  | 'hunt'
  | 'resist'
  | 'explore'
  | 'escape';

export interface SummonAssistantVariant {
  summonLevel: SummonAssistantLevel;
  creatureId: string;
  creatureName: string;
  template: SummonTemplateType | null;
  label: string;
}

export interface SummonAssistantProfile {
  key: SummonAssistantProfileKey;
  title: string;
  variants: SummonAssistantVariant[];
}

export interface SummonAssistantOption {
  label: string;
  nextNodeId?: string;
  profileKey?: SummonAssistantProfileKey;
}

export interface SummonAssistantNode {
  id: string;
  title: string;
  question: string;
  options: SummonAssistantOption[];
}

export interface SummonAssistantSuggestion {
  profileKey: SummonAssistantProfileKey;
  profileTitle: string;
  variant: SummonAssistantVariant;
}

export const summonAssistantProfiles: Record<SummonAssistantProfileKey, SummonAssistantProfile> = {
  damage_single: {
    key: 'damage_single',
    title: 'Daño a objetivo único',
    variants: [
      {
        summonLevel: 3,
        creatureId: 'lantern-archon',
        creatureName: 'Lantern Archon',
        template: null,
        label: 'Lantern Archon',
      },
      {
        summonLevel: 2,
        creatureId: 'dire-bat',
        creatureName: 'Dire Bat',
        template: 'CELESTIAL',
        label: 'Dire Bat celestial',
      },
      {
        summonLevel: 1,
        creatureId: 'wolf',
        creatureName: 'Wolf',
        template: 'FIERY',
        label: 'Wolf fiery',
      },
    ],
  },
  damage_many: {
    key: 'damage_many',
    title: 'Daño a varios enemigos',
    variants: [
      {
        summonLevel: 3,
        creatureId: 'small-earth-elemental',
        creatureName: 'Small Earth Elemental',
        template: 'CHTHONIC',
        label: 'Small Earth Elemental chthonic',
      },
      {
        summonLevel: 2,
        creatureId: 'dire-bat',
        creatureName: 'Dire Bat',
        template: 'FIERY',
        label: 'Dire Bat fiery',
      },
      {
        summonLevel: 1,
        creatureId: 'giant-frog',
        creatureName: 'Giant Frog',
        template: 'CHTHONIC',
        label: 'Giant Frog chthonic',
      },
    ],
  },
  tank: {
    key: 'tank',
    title: 'Aguantar o trabar',
    variants: [
      {
        summonLevel: 3,
        creatureId: 'small-earth-elemental',
        creatureName: 'Small Earth Elemental',
        template: 'RESOLUTE',
        label: 'Small Earth Elemental resolute',
      },
      {
        summonLevel: 2,
        creatureId: 'giant-frog',
        creatureName: 'Giant Frog',
        template: 'RESOLUTE',
        label: 'Giant Frog resolute',
      },
      {
        summonLevel: 1,
        creatureId: 'wolf',
        creatureName: 'Wolf',
        template: 'RESOLUTE',
        label: 'Wolf resolute',
      },
    ],
  },
  block: {
    key: 'block',
    title: 'Bloqueo y control de espacio',
    variants: [
      {
        summonLevel: 3,
        creatureId: 'small-earth-elemental',
        creatureName: 'Small Earth Elemental',
        template: 'CHTHONIC',
        label: 'Small Earth Elemental chthonic',
      },
      {
        summonLevel: 2,
        creatureId: 'dire-bat',
        creatureName: 'Dire Bat',
        template: 'RESOLUTE',
        label: 'Dire Bat resolute',
      },
      {
        summonLevel: 1,
        creatureId: 'wolf',
        creatureName: 'Wolf',
        template: 'CHTHONIC',
        label: 'Wolf chthonic',
      },
    ],
  },
  hunt: {
    key: 'hunt',
    title: 'Cazar objetivo difícil',
    variants: [
      {
        summonLevel: 3,
        creatureId: 'dire-bat',
        creatureName: 'Dire Bat',
        template: 'CELESTIAL',
        label: 'Dire Bat celestial',
      },
      {
        summonLevel: 2,
        creatureId: 'wolf',
        creatureName: 'Wolf',
        template: 'FIERY',
        label: 'Wolf fiery',
      },
      {
        summonLevel: 1,
        creatureId: 'giant-frog',
        creatureName: 'Giant Frog',
        template: 'CELESTIAL',
        label: 'Giant Frog celestial',
      },
    ],
  },
  resist: {
    key: 'resist',
    title: 'Sobrevivir a una amenaza concreta',
    variants: [
      {
        summonLevel: 3,
        creatureId: 'small-earth-elemental',
        creatureName: 'Small Earth Elemental',
        template: 'CHTHONIC',
        label: 'Small Earth Elemental chthonic',
      },
      {
        summonLevel: 2,
        creatureId: 'wolf',
        creatureName: 'Wolf',
        template: 'RESOLUTE',
        label: 'Wolf resolute',
      },
      {
        summonLevel: 1,
        creatureId: 'giant-frog',
        creatureName: 'Giant Frog',
        template: 'RESOLUTE',
        label: 'Giant Frog resolute',
      },
    ],
  },
  explore: {
    key: 'explore',
    title: 'Exploración y utilidades',
    variants: [
      {
        summonLevel: 3,
        creatureId: 'dire-bat',
        creatureName: 'Dire Bat',
        template: 'CELESTIAL',
        label: 'Dire Bat celestial',
      },
      {
        summonLevel: 2,
        creatureId: 'wolf',
        creatureName: 'Wolf',
        template: 'RESOLUTE',
        label: 'Wolf resolute',
      },
      {
        summonLevel: 1,
        creatureId: 'giant-frog',
        creatureName: 'Giant Frog',
        template: 'CELESTIAL',
        label: 'Giant Frog celestial',
      },
    ],
  },
  escape: {
    key: 'escape',
    title: 'Huida o emergencia',
    variants: [
      {
        summonLevel: 3,
        creatureId: 'dire-bat',
        creatureName: 'Dire Bat',
        template: 'CELESTIAL',
        label: 'Dire Bat celestial',
      },
      {
        summonLevel: 2,
        creatureId: 'wolf',
        creatureName: 'Wolf',
        template: 'FIERY',
        label: 'Wolf fiery',
      },
      {
        summonLevel: 1,
        creatureId: 'giant-frog',
        creatureName: 'Giant Frog',
        template: 'RESOLUTE',
        label: 'Giant Frog resolute',
      },
    ],
  },
};

export const summonAssistantNodes: Record<string, SummonAssistantNode> = {
  root: {
    id: 'root',
    title: 'Situación',
    question: '¿Qué situación quieres resolver?',
    options: [
      { label: 'Combate', nextNodeId: 'combat' },
      { label: 'Exploración', nextNodeId: 'exploration' },
      { label: 'Huida / emergencia', nextNodeId: 'escape' },
    ],
  },
  combat: {
    id: 'combat',
    title: 'Combate',
    question: '¿Cuál es tu objetivo principal en combate?',
    options: [
      { label: 'Hacer daño', nextNodeId: 'combat_damage' },
      { label: 'Aguantar / tanquear', nextNodeId: 'combat_tank' },
      { label: 'Bloquear o controlar espacio', nextNodeId: 'combat_block' },
      { label: 'Cazar objetivo difícil', nextNodeId: 'combat_hunt' },
      { label: 'Sobrevivir a una amenaza concreta', nextNodeId: 'combat_resist' },
    ],
  },
  combat_damage: {
    id: 'combat_damage',
    title: 'Hacer daño',
    question: '¿Qué tipo de daño necesitas hacer?',
    options: [
      { label: 'A un enemigo fuerte', nextNodeId: 'combat_damage_strong' },
      { label: 'A muchos enemigos débiles', nextNodeId: 'combat_damage_many' },
      { label: 'A un enemigo difícil de alcanzar', nextNodeId: 'combat_damage_hard_to_reach' },
    ],
  },
  combat_damage_strong: {
    id: 'combat_damage_strong',
    title: 'A un enemigo fuerte',
    question: '¿Qué rasgo te preocupa más?',
    options: [
      { label: 'CA alta', profileKey: 'damage_single' },
      { label: 'RD', profileKey: 'damage_single' },
      { label: 'Muchos PG', profileKey: 'damage_single' },
      { label: 'Resistencia elemental', profileKey: 'damage_single' },
      { label: 'No lo sé', profileKey: 'damage_single' },
    ],
  },
  combat_damage_many: {
    id: 'combat_damage_many',
    title: 'A muchos enemigos débiles',
    question: '¿Cómo están distribuidos?',
    options: [
      { label: 'Agrupados', profileKey: 'damage_many' },
      { label: 'Dispersos', profileKey: 'damage_many' },
      { label: 'Mezclados con aliados', profileKey: 'damage_many' },
      { label: 'No lo sé', profileKey: 'damage_many' },
    ],
  },
  combat_damage_hard_to_reach: {
    id: 'combat_damage_hard_to_reach',
    title: 'A un enemigo difícil de alcanzar',
    question: '¿Qué hace difícil llegar hasta él?',
    options: [
      { label: 'Vuela', profileKey: 'damage_single' },
      { label: 'Está lejos', profileKey: 'damage_single' },
      { label: 'Está tras enemigos', profileKey: 'damage_single' },
      { label: 'Está en altura', profileKey: 'damage_single' },
      { label: 'Está en agua', profileKey: 'damage_single' },
      { label: 'Está tras obstáculos', profileKey: 'damage_single' },
    ],
  },
  combat_tank: {
    id: 'combat_tank',
    title: 'Aguantar o tanquear',
    question: '¿Qué quieres absorber o frenar?',
    options: [
      { label: 'Un enemigo fuerte', nextNodeId: 'combat_tank_one' },
      { label: 'Muchos enemigos', nextNodeId: 'combat_tank_many' },
      { label: 'Daño elemental', nextNodeId: 'combat_tank_elemental' },
    ],
  },
  combat_tank_one: {
    id: 'combat_tank_one',
    title: 'Un enemigo fuerte',
    question: '¿Qué necesitas hacer exactamente?',
    options: [
      { label: 'Trabar', profileKey: 'tank' },
      { label: 'Sobrevivir', profileKey: 'tank' },
      { label: 'Gastar acciones', profileKey: 'tank' },
      { label: 'Proteger aliado', profileKey: 'tank' },
    ],
  },
  combat_tank_many: {
    id: 'combat_tank_many',
    title: 'Muchos enemigos',
    question: '¿Qué efecto quieres provocar?',
    options: [
      { label: 'Ocupar casillas', profileKey: 'tank' },
      { label: 'Dividir ataques', profileKey: 'tank' },
      { label: 'Cuello de botella', profileKey: 'tank' },
      { label: 'Aguantar ataques pequeños', profileKey: 'tank' },
    ],
  },
  combat_tank_elemental: {
    id: 'combat_tank_elemental',
    title: 'Daño elemental',
    question: '¿Qué elemento esperas?',
    options: [
      { label: 'Fuego', profileKey: 'resist' },
      { label: 'Frío', profileKey: 'resist' },
      { label: 'Ácido', profileKey: 'resist' },
      { label: 'Electricidad', profileKey: 'resist' },
      { label: 'No lo sé', profileKey: 'resist' },
    ],
  },
  combat_block: {
    id: 'combat_block',
    title: 'Bloquear o controlar espacio',
    question: '¿Qué quieres impedir?',
    options: [
      { label: 'Que pasen', profileKey: 'block' },
      { label: 'Que carguen', profileKey: 'block' },
      { label: 'Que lleguen a un aliado', profileKey: 'block' },
      { label: 'Que huyan', profileKey: 'block' },
      { label: 'Que rodeen', profileKey: 'block' },
    ],
  },
  combat_hunt: {
    id: 'combat_hunt',
    title: 'Cazar objetivo difícil',
    question: '¿Qué tipo de objetivo quieres perseguir?',
    options: [
      { label: 'Volador', profileKey: 'hunt' },
      { label: 'Lanzador', profileKey: 'hunt' },
      { label: 'Arquero', profileKey: 'hunt' },
      { label: 'Móvil', profileKey: 'hunt' },
      { label: 'Oculto', profileKey: 'hunt' },
      { label: 'Tras línea frontal', profileKey: 'hunt' },
    ],
  },
  combat_resist: {
    id: 'combat_resist',
    title: 'Sobrevivir a amenaza concreta',
    question: '¿Qué amenaza concreta esperas?',
    options: [
      { label: 'Fuego', profileKey: 'resist' },
      { label: 'Frío', profileKey: 'resist' },
      { label: 'Ácido', profileKey: 'resist' },
      { label: 'Electricidad', profileKey: 'resist' },
      { label: 'Veneno / enfermedad', profileKey: 'resist' },
      { label: 'Mente / miedo', profileKey: 'resist' },
      { label: 'Daño físico', profileKey: 'resist' },
      { label: 'Áreas', profileKey: 'resist' },
    ],
  },
  exploration: {
    id: 'exploration',
    title: 'Exploración',
    question: '¿Qué quieres hacer fuera del combate directo?',
    options: [
      { label: 'Buscar / rastrear', profileKey: 'explore' },
      { label: 'Zona peligrosa', profileKey: 'explore' },
      { label: 'Infiltrarse', profileKey: 'explore' },
      { label: 'Terreno especial', profileKey: 'explore' },
      { label: 'Activar algo peligroso', profileKey: 'explore' },
      { label: 'Transportar', profileKey: 'explore' },
    ],
  },
  escape: {
    id: 'escape',
    title: 'Huida / emergencia',
    question: '¿Qué necesitas conseguir ahora mismo?',
    options: [
      { label: 'Ganar tiempo', profileKey: 'escape' },
      { label: 'Bloquear perseguidores', profileKey: 'escape' },
      { label: 'Distraer', profileKey: 'escape' },
      { label: 'Rescatar / transportar', profileKey: 'escape' },
      { label: 'Cubrir retirada', profileKey: 'escape' },
      { label: 'Romper contacto', profileKey: 'escape' },
    ],
  },
};

export function getSummonAssistantSuggestion(profileKey: SummonAssistantProfileKey, maxSummonMonsterLevel: number): SummonAssistantSuggestion {
  const profile = summonAssistantProfiles[profileKey];
  const normalizedLevel = Math.max(1, Math.min(3, Math.trunc(maxSummonMonsterLevel) || 1)) as SummonAssistantLevel;
  const candidates = profile.variants.filter(variant => variant.summonLevel <= normalizedLevel);
  const variant = (candidates.sort((left, right) => right.summonLevel - left.summonLevel)[0] ?? profile.variants[profile.variants.length - 1]) as SummonAssistantVariant;

  return {
    profileKey: profile.key,
    profileTitle: profile.title,
    variant,
  };
}
