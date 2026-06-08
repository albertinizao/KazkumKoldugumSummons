import { defineStore } from 'pinia';
import {
  clearCombatState,
  clearLastRollResult,
  decreaseDailyUses as decreaseDailyUsesApi,
  damageInstance,
  fetchCatalogCreatures,
  fetchCombatState,
  fetchConfiguration,
  healInstance,
  increaseDailyUses as increaseDailyUsesApi,
  removeInstance,
  resetDailyUses as resetDailyUsesApi,
  rollGroupAttacks as rollGroupAttacksApi,
  rollGroupSavingThrows as rollGroupSavingThrowsApi,
  summonCreature,
  type DailyUsesState,
} from '@/services/combatApi';
import type { CreatureCatalogItem, SummonTemplateType } from '@/types/catalog';
import type { CombatRollResult, CombatState, SummonShortcut } from '@/types/combat';

const defaultCombatState: CombatState = {
  activeGroups: [],
  dailyUses: {
    maximum: 6,
    remaining: 4,
  },
  configuration: {
    maxSummonMonsterLevel: 3,
    availableTemplates: ['CHTHONIC', 'FIERY', 'CELESTIAL', 'ENTROPIC', 'RESOLUTE'],
  },
  lastRollResult: null,
  recentlyUsedSummons: [],
  mostUsedSummons: [],
};

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
    combatState: defaultCombatState as CombatState,
    lastCombatRollResult: null as CombatRollResult | null,
    catalogItems: [] as CreatureCatalogItem[],
    selectedCreatureId: null as string | null,
    selectedTemplate: null as SummonTemplateType | null,
    loading: false,
    busy: false,
    error: '' as string,
    initialized: false,
    dailyUsesLoading: false,
    dailyUsesError: null as string | null,
  }),
  getters: {
    groups: state => state.combatState.activeGroups,
    dailyUses: state => state.combatState.dailyUses,
    configuration: state => state.combatState.configuration,
    lastRollResult: state => state.combatState.lastRollResult,
    recentlyUsedSummons: state => state.combatState.recentlyUsedSummons,
    mostUsedSummons: state => state.combatState.mostUsedSummons,
    selectedCreature: state => state.catalogItems.find(item => item.id === state.selectedCreatureId) ?? null,
    selectedCreatureAllowedTemplates: state => state.catalogItems.find(item => item.id === state.selectedCreatureId)?.allowedTemplates ?? [],
    activeInstanceCount: state => state.combatState.activeGroups.reduce((total, group) => total + group.instances.length, 0),
  },
  actions: {
    async initialize() {
      if (this.initialized) {
        return;
      }

      this.loading = true;
      this.error = '';

      try {
        const [combatState, catalog] = await Promise.all([
          fetchCombatState(),
          fetchCatalogCreatures(),
        ]);

        this.combatState = {
          ...combatState,
          dailyUses: normalizeDailyUses(combatState.dailyUses),
        };
        this.lastCombatRollResult = null;
        this.catalogItems = catalog.items;
        this.initialized = true;

        if (!this.selectedCreatureId && this.catalogItems.length > 0) {
          this.selectCreature(this.catalogItems[0].id);
        } else if (this.selectedCreatureId) {
          this.ensureSelectedTemplateIsAllowed();
        }
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo cargar el estado de combate.';
      } finally {
        this.loading = false;
      }
    },
    async refreshCombatState() {
      const combatState = await fetchCombatState();
      this.combatState = {
        ...combatState,
        dailyUses: normalizeDailyUses(combatState.dailyUses),
      };
      this.ensureSelectedTemplateIsAllowed();
    },
    selectCreature(creatureId: string) {
      this.selectedCreatureId = creatureId;
      const creature = this.selectedCreature;

      if (!creature) {
        this.selectedTemplate = null;
        return;
      }

      this.selectedTemplate = creature.allowedTemplates.length === 1 ? creature.allowedTemplates[0] : null;
    },
    selectTemplate(template: SummonTemplateType | null) {
      this.selectedTemplate = template;
    },
    ensureSelectedTemplateIsAllowed() {
      const creature = this.selectedCreature;
      if (!creature) {
        this.selectedTemplate = null;
        return;
      }

      if (this.selectedTemplate && creature.allowedTemplates.includes(this.selectedTemplate)) {
        return;
      }

      this.selectedTemplate = creature.allowedTemplates.length === 1 ? creature.allowedTemplates[0] : null;
    },
    async summonSelectedCreature() {
      const creature = this.selectedCreature;
      if (!creature) {
        this.error = 'Selecciona una criatura antes de invocar.';
        return;
      }

      if (creature.allowedTemplates.length > 1 && !this.selectedTemplate) {
        this.error = 'Elige una plantilla antes de invocar.';
        return;
      }

      this.busy = true;
      this.error = '';

      try {
        this.combatState = await summonCreature({
          creatureTemplateId: creature.id,
          selectedTemplate: this.selectedTemplate,
          source: 'MANUAL_SEARCH',
        });
        this.ensureSelectedTemplateIsAllowed();
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo invocar la criatura.';
      } finally {
        this.busy = false;
      }
    },
    async summonFromShortcut(shortcut: SummonShortcut, source: 'RECENT' | 'MOST_USED') {
      this.busy = true;
      this.error = '';

      try {
        this.combatState = await summonCreature({
          shortcutId: shortcut.id,
          source,
          selectedTemplate: shortcut.selectedTemplate,
        });
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo invocar desde el acceso rápido.';
      } finally {
        this.busy = false;
      }
    },
    async clearSummons() {
      this.busy = true;
      this.error = '';

      try {
        this.combatState = await clearCombatState();
        this.lastCombatRollResult = null;
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudieron limpiar las invocaciones.';
      } finally {
        this.busy = false;
      }
    },
    async damageCreature(instanceId: string, amount: number) {
      this.busy = true;
      this.error = '';

      try {
        this.combatState = await damageInstance(instanceId, amount);
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo aplicar el daño.';
      } finally {
        this.busy = false;
      }
    },
    async healCreature(instanceId: string, amount: number) {
      this.busy = true;
      this.error = '';

      try {
        this.combatState = await healInstance(instanceId, amount);
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo aplicar la curación.';
      } finally {
        this.busy = false;
      }
    },
    async removeCreature(instanceId: string) {
      this.busy = true;
      this.error = '';

      try {
        this.combatState = await removeInstance(instanceId);
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo eliminar la criatura.';
      } finally {
        this.busy = false;
      }
    },
    async clearLastRoll() {
      this.busy = true;
      this.error = '';

      try {
        this.combatState = await clearLastRollResult();
        this.lastCombatRollResult = null;
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo limpiar el último resultado.';
      } finally {
        this.busy = false;
      }
    },
    async rollGroupAttacks(groupId: string) {
      this.busy = true;
      this.error = '';

      try {
        const response = await rollGroupAttacksApi(groupId);
        this.combatState = response.combatState;
        this.lastCombatRollResult = response.rollResult;
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo resolver el ataque del grupo.';
      } finally {
        this.busy = false;
      }
    },
    async rollGroupSavingThrows(groupId: string) {
      this.busy = true;
      this.error = '';

      try {
        const response = await rollGroupSavingThrowsApi(groupId);
        this.combatState = response.combatState;
        this.lastCombatRollResult = response.rollResult;
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudieron tirar las TS del grupo.';
      } finally {
        this.busy = false;
      }
    },
    async loadDailyUses() {
      this.dailyUsesLoading = true;
      this.dailyUsesError = null;
      try {
        const configuration = await fetchConfiguration();
        this.combatState = {
          ...this.combatState,
          dailyUses: normalizeDailyUses(configuration.dailyUses),
        };
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
        this.combatState = {
          ...this.combatState,
          dailyUses: normalizeDailyUses(response.dailyUses),
        };
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
        this.combatState = {
          ...this.combatState,
          dailyUses: normalizeDailyUses(response.dailyUses),
        };
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
        this.combatState = {
          ...this.combatState,
          dailyUses: normalizeDailyUses(response.dailyUses),
        };
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
