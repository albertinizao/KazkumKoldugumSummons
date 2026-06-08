import { defineStore } from 'pinia';
import { fetchConfiguration, type DailyUsesState } from '@/services/combatApi';
import { saveConfiguration } from '@/services/api';
import { useCombatStore } from '@/stores/combat';

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    maxSummonMonsterLevel: 3,
    dailyUsesMaximum: 6,
    dailyUses: {
      maximum: 6,
      remaining: 4,
    } as DailyUsesState,
    loading: false,
    saving: false,
    loaded: false,
    error: null as string | null,
    lastSavedAt: null as string | null,
  }),
  getters: {
    displayMaxSummonMonsterLevel: state => state.maxSummonMonsterLevel,
  },
  actions: {
    async loadConfiguration() {
      this.loading = true;
      this.error = null;
      try {
        const configuration = await fetchConfiguration();
        this.maxSummonMonsterLevel = configuration.maxSummonMonsterLevel;
        this.dailyUsesMaximum = configuration.dailyUses.maximum;
        this.dailyUses = configuration.dailyUses;
        this.loaded = true;
      } catch (error) {
        this.error = error instanceof Error ? error.message : String(error);
      } finally {
        this.loading = false;
      }
    },
    async updateConfiguration(maxSummonMonsterLevel: number, dailyUsesMaximum: number) {
      this.saving = true;
      this.error = null;
      try {
        const configuration = await saveConfiguration(maxSummonMonsterLevel, dailyUsesMaximum);
        this.maxSummonMonsterLevel = configuration.maxSummonMonsterLevel;
        this.dailyUsesMaximum = configuration.dailyUses.maximum;
        this.dailyUses = configuration.dailyUses;
        this.loaded = true;
        this.lastSavedAt = new Date().toISOString();
        await useCombatStore().refreshCombatState();
        return configuration;
      } catch (error) {
        this.error = error instanceof Error ? error.message : String(error);
        throw error;
      } finally {
        this.saving = false;
      }
    },
  },
});
