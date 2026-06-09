export type Alignment = 'LG' | 'LN' | 'LE' | 'NG' | 'N' | 'NE' | 'CG' | 'CN' | 'CE';

export type CreatureSize = 'FINE' | 'DIMINUTIVE' | 'TINY' | 'SMALL' | 'MEDIUM' | 'LARGE' | 'HUGE' | 'GARGANTUAN' | 'COLOSSAL';

export type SummonTemplateType = 'CHTHONIC' | 'FIERY' | 'CELESTIAL' | 'ENTROPIC' | 'RESOLUTE';

export interface ArmorClass {
  normal: number;
  touch: number;
  flatFooted: number;
  detail?: string | null;
}

export interface SavingThrows {
  fortitude: number;
  reflex: number;
  will: number;
}

export interface CreatureCatalogSummary {
  armorClass: ArmorClass;
  maxHitPoints: number;
  hitPointsFormula?: string | null;
  cmb?: number | null;
  cmd?: number | null;
  savingThrows: SavingThrows;
  speedsText: string;
  attacksText: string;
}

export interface CreatureCatalogItem {
  id: string;
  name: string;
  summonLevel: number;
  alignment: Alignment;
  size: CreatureSize;
  creatureType: string;
  subtypes: string[];
  allowedTemplates: SummonTemplateType[];
  summary: CreatureCatalogSummary;
}

export interface CreatureCatalogListResponse {
  items: CreatureCatalogItem[];
  total: number;
}

export interface AbilityScores {
  strength: number;
  dexterity: number;
  constitution: number;
  intelligence: number;
  wisdom: number;
  charisma: number;
}

export interface Speed {
  type: 'LAND' | 'FLY' | 'SWIM' | 'CLIMB' | 'BURROW' | 'OTHER';
  valueFeet: number;
  maneuverability?: string | null;
  notes?: string | null;
}

export interface DamageComponent {
  formula: string;
  damageType: 'PIERCING' | 'SLASHING' | 'BLUDGEONING' | 'FIRE' | 'COLD' | 'ACID' | 'ELECTRICITY' | 'SONIC' | 'FORCE' | 'UNTYPED' | 'OTHER';
  multipliesOnCritical: boolean;
  damageAbility?: 'STRENGTH' | 'DEXTERITY' | 'NONE' | 'OTHER' | null;
  damageAbilityMultiplier?: number | null;
  label?: string | null;
}

export interface CriticalProfile {
  threatRangeStart: number;
  multiplier: number;
}

export interface Attack {
  id: string;
  name: string;
  attackBonus: number;
  attackAbility?: 'STRENGTH' | 'DEXTERITY' | 'NONE' | 'OTHER' | null;
  quantity: number;
  attackType: 'MELEE' | 'RANGED' | 'TOUCH' | 'SPECIAL';
  damageComponents: DamageComponent[];
  critical?: CriticalProfile | null;
  notes: string[];
}

export interface SpecialDefense {
  type: 'DAMAGE_REDUCTION' | 'RESISTANCE' | 'IMMUNITY' | 'SPELL_RESISTANCE' | 'VULNERABILITY' | 'OTHER';
  value?: string | null;
  notes?: string | null;
}

export interface AbilitySummary {
  name: string;
  summary: string;
}

export interface AbilityDetail {
  name: string;
  text: string;
}

export interface CreatureTemplate {
  id: string;
  name: string;
  summonLevel: number;
  alignment: Alignment;
  size: CreatureSize;
  creatureType: string;
  subtypes: string[];
  allowedTemplates: SummonTemplateType[];
  initiative: number;
  senses: string[];
  perception: number;
  abilities: AbilityScores;
  armorClass: ArmorClass;
  hitPoints: {
    maximum: number;
    formula: string;
    hitDice?: { count: number; dieSize: number } | null;
  };
  savingThrows: SavingThrows;
  speeds: Speed[];
  attacks: Attack[];
  space: string;
  reach: string;
  specialAttacks: string[];
  specialDefenses: SpecialDefense[];
  tacticalNotes: string[];
  shortAbilities: AbilitySummary[];
  expandedAbilities: AbilityDetail[];
  fullStatBlock: string;
}

export interface AppliedRule {
  type: 'AUGMENT_SUMMONING' | 'SUPERIOR_SUMMONING' | 'VERSATILE_SUMMON_MONSTER' | 'DEEP_GUARDIAN';
  description: string;
}

export interface ResolvedCreature {
  id: string;
  baseTemplateId: string;
  displayName: string;
  summonLevel: number;
  appliedTemplate: SummonTemplateType | null;
  alignment: Alignment;
  size: CreatureSize;
  creatureType: string;
  subtypes: string[];
  initiative: number;
  senses: string[];
  perception: number;
  armorClass: ArmorClass;
  maxHitPoints: number;
  hitPointsFormula?: string | null;
  cmb: number;
  cmd: number;
  savingThrows: SavingThrows;
  speeds: Speed[];
  speedsText: string;
  attacks: Attack[];
  attacksText: string;
  space: string;
  reach: string;
  specialAttacks: string[];
  specialDefenses: SpecialDefense[];
  shortAbilities: AbilitySummary[];
  expandedAbilities: AbilityDetail[];
  fullStatBlock: string;
  appliedRules: AppliedRule[];
}
