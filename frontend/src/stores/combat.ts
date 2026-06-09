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
import { summonTemplates } from '@/data/summonTemplates';
import { readCombatStateSnapshot, writeCombatStateSnapshot } from '@/utils/combatStatePersistence';

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

function normalizeCombatState(combatState: CombatState): CombatState {
  return {
    ...combatState,
    dailyUses: normalizeDailyUses(combatState.dailyUses),
  };
}

export const useCombatStore = defineStore('combat', {
  state: () => ({
    combatState: defaultCombatState as CombatState,
    lastCombatRollResult: null as CombatRollResult | null,
    catalogItems: [] as CreatureCatalogItem[],
    catalogLevelFilter: null as number | null,
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
    filteredCatalogItems: state => state.catalogItems.filter(item => state.catalogLevelFilter === null || item.summonLevel === state.catalogLevelFilter),
    selectedCreature: state => state.catalogItems.find(item => item.id === state.selectedCreatureId) ?? null,
    selectedCreatureAllowedTemplates: state => {
      const creature = state.catalogItems.find(item => item.id === state.selectedCreatureId);
      if (!creature) {
        return [];
      }

      const creatureType = creature.creatureType.toLowerCase();
      if (creatureType.includes('outsider')) {
        return [];
      }

      return summonTemplates
        .filter(template => template.key !== 'none')
        .map(template => template.key.toUpperCase() as SummonTemplateType);
    },
    activeInstanceCount: state => state.combatState.activeGroups.reduce((total, group) => total + group.instances.length, 0),
  },
  actions: {
    async initialize() {
      if (this.initialized) {
        return;
      }

      this.loading = true;
      this.error = '';
      const cachedCombatState = readCombatStateSnapshot(defaultCombatState);
      if (cachedCombatState) {
        this.combatState = cachedCombatState;
      }

      try {
        const [combatStateResult, catalogResult] = await Promise.allSettled([
          fetchCombatState(),
          fetchCatalogCreatures({ limit: 1000, offset: 0 }),
        ]);

        if (combatStateResult.status === 'fulfilled') {
          this.combatState = normalizeCombatState(combatStateResult.value);
          writeCombatStateSnapshot(this.combatState);
        } else if (!cachedCombatState) {
          throw combatStateResult.reason;
        }

        if (catalogResult.status === 'fulfilled') {
          this.catalogItems = catalogResult.value.items;
        } else if (!cachedCombatState) {
          throw catalogResult.reason;
        }

        this.lastCombatRollResult = null;
        this.initialized = true;

        if (this.catalogLevelFilter === null) {
          this.catalogLevelFilter = this.combatState.configuration.maxSummonMonsterLevel;
        }

        this.ensureSelectedCreatureMatchesFilter();

        if (this.selectedCreatureId) {
          this.ensureSelectedTemplateIsAllowed();
        }
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo cargar el estado de combate.';
        this.initialized = true;
      } finally {
        this.loading = false;
      }
    },
    async refreshCombatState() {
      const combatState = await fetchCombatState();
      this.combatState = normalizeCombatState(combatState);
      writeCombatStateSnapshot(this.combatState);
      this.ensureSelectedTemplateIsAllowed();
    },
    selectCreature(creatureId: string) {
      this.selectedCreatureId = creatureId;
      const creature = this.selectedCreature;

      if (!creature) {
        this.selectedTemplate = null;
        return;
      }

      this.selectedTemplate = this.selectedCreatureAllowedTemplates.length === 1 ? this.selectedCreatureAllowedTemplates[0] : null;
    },
    selectCatalogLevel(level: number | null) {
      this.catalogLevelFilter = level;
      this.ensureSelectedCreatureMatchesFilter();
    },
    selectTemplate(template: SummonTemplateType | null) {
      this.selectedTemplate = template;
    },
    ensureSelectedCreatureMatchesFilter() {
      const filteredCatalogItems = this.filteredCatalogItems;

      if (filteredCatalogItems.length === 0) {
        this.selectedCreatureId = null;
        this.selectedTemplate = null;
        return;
      }

      const selectedCreatureStillVisible = filteredCatalogItems.some(item => item.id === this.selectedCreatureId);
      if (!selectedCreatureStillVisible) {
        this.selectCreature(filteredCatalogItems[0].id);
      }
    },
    ensureSelectedTemplateIsAllowed() {
      const creature = this.selectedCreature;
      if (!creature) {
        this.selectedTemplate = null;
        return;
      }

      if (this.selectedTemplate && this.selectedCreatureAllowedTemplates.includes(this.selectedTemplate)) {
        return;
      }

      this.selectedTemplate = this.selectedCreatureAllowedTemplates.length === 1 ? this.selectedCreatureAllowedTemplates[0] : null;
    },
    async summonSelectedCreature() {
      const creature = this.selectedCreature;
      if (!creature) {
        this.error = 'Selecciona una criatura antes de invocar.';
        return;
      }

      if (this.selectedCreatureAllowedTemplates.length > 1 && !this.selectedTemplate) {
        this.error = 'Elige una plantilla antes de invocar.';
        return;
      }

      this.busy = true;
      this.error = '';

      try {
        this.combatState = normalizeCombatState(await summonCreature({
          creatureTemplateId: creature.id,
          selectedTemplate: this.selectedTemplate,
          source: 'MANUAL_SEARCH',
        }));
        writeCombatStateSnapshot(this.combatState);
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
        this.combatState = normalizeCombatState(await summonCreature({
          shortcutId: shortcut.id,
          source,
          selectedTemplate: shortcut.selectedTemplate,
        }));
        writeCombatStateSnapshot(this.combatState);
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
        this.combatState = normalizeCombatState(await clearCombatState());
        writeCombatStateSnapshot(this.combatState);
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
        this.combatState = normalizeCombatState(await damageInstance(instanceId, amount));
        writeCombatStateSnapshot(this.combatState);
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
        this.combatState = normalizeCombatState(await healInstance(instanceId, amount));
        writeCombatStateSnapshot(this.combatState);
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
        this.combatState = normalizeCombatState(await removeInstance(instanceId));
        writeCombatStateSnapshot(this.combatState);
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
        this.combatState = normalizeCombatState(await clearLastRollResult());
        writeCombatStateSnapshot(this.combatState);
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
        this.combatState = normalizeCombatState(response.combatState);
        writeCombatStateSnapshot(this.combatState);
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
        this.combatState = normalizeCombatState(response.combatState);
        writeCombatStateSnapshot(this.combatState);
        this.lastCombatRollResult = response.rollResult;
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudieron tirar las TS del grupo.';
      } finally {
        this.busy = false;
      }
    },
    async rollAllGroupAttacks(): Promise<{ title: string; displayText: string } | null> {
      const groups = [...this.groups];
      if (groups.length === 0) {
        return null;
      }

      this.busy = true;
      this.error = '';

      try {
        const results = await Promise.all(
          groups.map(group => rollGroupAttacksApi(group.id)),
        );

        const lastResponse = results[results.length - 1];
        if (lastResponse) {
          this.combatState = normalizeCombatState(lastResponse.combatState);
          writeCombatStateSnapshot(this.combatState);
        }

        return {
          title: 'Atacar con todas: todos los grupos',
          displayText: results
            .map(response => response.rollResult.displayText)
            .join('\n\n'),
        };
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudieron resolver los ataques globales.';
        return null;
      } finally {
        this.busy = false;
      }
    },
    async rollAllGroupSavingThrows(): Promise<{ title: string; displayText: string } | null> {
      const groups = [...this.groups];
      if (groups.length === 0) {
        return null;
      }

      this.busy = true;
      this.error = '';

      try {
        const results = await Promise.all(
          groups.map(group => rollGroupSavingThrowsApi(group.id)),
        );

        const lastResponse = results[results.length - 1];
        if (lastResponse) {
          this.combatState = normalizeCombatState(lastResponse.combatState);
          writeCombatStateSnapshot(this.combatState);
        }

        return {
          title: 'Tirar TS con todas: todos los grupos',
          displayText: results
            .map(response => response.rollResult.displayText)
            .join('\n\n'),
        };
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudieron resolver las salvaciones globales.';
        return null;
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
        writeCombatStateSnapshot(this.combatState);
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
        writeCombatStateSnapshot(this.combatState);
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
        writeCombatStateSnapshot(this.combatState);
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
        writeCombatStateSnapshot(this.combatState);
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
