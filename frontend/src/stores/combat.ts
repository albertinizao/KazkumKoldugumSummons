import { defineStore } from 'pinia';
import {
  decreaseDailyUses as decreaseDailyUsesApi,
  fetchConfiguration,
  increaseDailyUses as increaseDailyUsesApi,
  resetDailyUses as resetDailyUsesApi,
  type DailyUsesState,
} from '@/services/combatApi';

export type CreatureInstanceState = 'HEALTHY' | 'DAMAGED' | 'DOWN';

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

function clamp(value: number, minimum: number, maximum: number): number {
  return Math.min(maximum, Math.max(minimum, value));
}

function normalizeDailyUses(dailyUses: DailyUsesState): DailyUsesState {
  const maximum = Math.max(0, Number(dailyUses.maximum ?? 0));
  const remaining = clamp(Number(dailyUses.remaining ?? 0), 0, maximum);
  return { maximum, remaining };
}

export const useCombatStore = defineStore('combat', {
  state: () => ({
    dailyUses: {
      maximum: 6,
      remaining: 4,
    } as DailyUsesState,
    dailyUsesLoading: false,
    dailyUsesError: null as string | null,
    lastRollResult: '' as string,
    groups: [] as CombatGroupSummary[],
    expandedGroupId: null as string | null,
    isSummonModalOpen: false,
    selectedInstanceId: null as string | null,
    activeInstancesByGroup: {} as Record<string, CombatInstanceSummary[]>,
  }),
  actions: {
    async loadDailyUses() {
      this.dailyUsesLoading = true;
      this.dailyUsesError = null;
      try {
        const configuration = await fetchConfiguration();
        this.dailyUses = normalizeDailyUses(configuration.dailyUses);
      } catch (error) {
        this.dailyUsesError = error instanceof Error ? error.message : String(error);
      } finally {
        this.dailyUsesLoading = false;
      }
    },
    async increaseDailyUses(amount = 1) {
      this.dailyUsesLoading = true;
      this.dailyUsesError = null;
      try {
        const response = await increaseDailyUsesApi(amount);
        this.dailyUses = normalizeDailyUses(response.dailyUses);
        return response;
      } catch (error) {
        this.dailyUsesError = error instanceof Error ? error.message : String(error);
        throw error;
      } finally {
        this.dailyUsesLoading = false;
      }
    },
    async decreaseDailyUses(amount = 1) {
      this.dailyUsesLoading = true;
      this.dailyUsesError = null;
      try {
        const response = await decreaseDailyUsesApi(amount);
        this.dailyUses = normalizeDailyUses(response.dailyUses);
        return response;
      } catch (error) {
        this.dailyUsesError = error instanceof Error ? error.message : String(error);
        throw error;
      } finally {
        this.dailyUsesLoading = false;
      }
    },
    async resetDailyUses() {
      this.dailyUsesLoading = true;
      this.dailyUsesError = null;
      try {
        const response = await resetDailyUsesApi();
        this.dailyUses = normalizeDailyUses(response.dailyUses);
        return response;
      } catch (error) {
        this.dailyUsesError = error instanceof Error ? error.message : String(error);
        throw error;
      } finally {
        this.dailyUsesLoading = false;
      }
    },
  },
});
