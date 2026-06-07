import { defineStore } from 'pinia';
import { clearCombatState, clearLastRollResult, damageInstance, fetchCatalogCreatures, fetchCombatState, healInstance, removeInstance, summonCreature } from '@/services/combatApi';
import type { CreatureCatalogItem, SummonTemplateType } from '@/types/catalog';
import type { CombatState, SummonShortcut } from '@/types/combat';

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

export const useCombatStore = defineStore('combat', {
  state: () => ({
    combatState: defaultCombatState as CombatState,
    catalogItems: [] as CreatureCatalogItem[],
    selectedCreatureId: null as string | null,
    selectedTemplate: null as SummonTemplateType | null,
    loading: false,
    busy: false,
    error: '' as string,
    initialized: false,
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

        this.combatState = combatState;
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
      this.combatState = await fetchCombatState();
      this.ensureSelectedTemplateIsAllowed();
    },
    selectCreature(creatureId: string) {
      this.selectedCreatureId = creatureId;
      this.ensureSelectedTemplateIsAllowed();
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

      this.selectedTemplate = creature.allowedTemplates[0] ?? null;
    },
    async summonSelectedCreature() {
      const creature = this.selectedCreature;
      if (!creature) {
        this.error = 'Selecciona una criatura antes de invocar.';
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
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'No se pudo limpiar el último resultado.';
      } finally {
        this.busy = false;
      }
    },
  },
});
