import { defineStore } from 'pinia';

export type CreatureInstanceState = 'HEALTHY' | 'DAMAGED' | 'DOWN';

export interface DailyUsesState {
  maximum: number;
  remaining: number;
}

export interface CombatGroupSummary {
  id: string;
  name: string;
  count: number;
  alignment: string;
  size: string;
  creatureType: string;
  initiative: string;
  senses: string;
  perception: string;
  armorClass: string;
  touchArmorClass: string;
  flatFootedArmorClass: string;
  hitPoints: string;
  fortitude: string;
  reflexes: string;
  will: string;
  speeds: string[];
  attacks: string[];
  space: string;
  reach: string;
  specialAttacks: string[];
}

export interface CombatInstanceSummary {
  id: string;
  label: string;
  currentHitPoints: number;
  maxHitPoints: number;
  state: CreatureInstanceState;
}

export const useCombatStore = defineStore('combat', {
  state: () => ({
    dailyUses: {
      maximum: 6,
      remaining: 4,
    } as DailyUsesState,
    lastRollResult: '' as string,
    groups: [] as CombatGroupSummary[],
    expandedGroupId: null as string | null,
    isSummonModalOpen: false,
    selectedInstanceId: null as string | null,
    activeInstancesByGroup: {} as Record<string, CombatInstanceSummary[]>,
  }),
});
