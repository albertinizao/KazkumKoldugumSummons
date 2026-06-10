<template>
  <section class="invocations-view">
    <div v-if="store.error" class="error-banner">{{ store.error }}</div>

    <div class="top-grid">
      <DailyUsesPanel />

      <section class="summon-panel card">
        <div class="section-title">
          <div>
            <p class="eyebrow">Catálogo</p>
            <h2>Preparar invocación</h2>
          </div>
          <StatusBadge variant="neutral">{{ store.configuration.maxSummonMonsterLevel }}</StatusBadge>
        </div>

        <div class="toolbar">
          <label class="level-filter">
            <span class="field-label">Nivel</span>
            <select class="level-filter__select" :value="store.catalogLevelFilter ?? ''" @change="onLevelFilterChange">
              <option :value="''">Todos</option>
              <option v-for="level in summonLevels" :key="level" :value="level">
                {{ level }}
              </option>
            </select>
          </label>

          <label>
            <span class="field-label">Criatura</span>
            <select :value="store.selectedCreatureId ?? ''" @change="onCreatureChange">
              <option v-for="item in store.filteredCatalogItems" :key="item.id" :value="item.id">
                {{ item.name }} · nivel {{ item.summonLevel }}
              </option>
            </select>
          </label>

          <label>
            <span class="field-label">Plantilla</span>
            <select :value="store.selectedTemplate ?? ''" @change="onTemplateChange">
              <option v-if="templateSelectionRequired" :value="''">Elige plantilla</option>
              <option v-else-if="allowedTemplates.length === 0" :value="''">Sin plantilla</option>
              <option v-for="template in allowedTemplates" :key="template" :value="template">
                {{ templateLabel(template) }}
              </option>
            </select>
          </label>

          <div class="button-stack">
            <ActionButton :disabled="store.busy || !canSummon" @click="handleSummon">
              Invocar
            </ActionButton>
            <ActionButton :disabled="store.busy" variant="neutral" @click="openSummonAssistant">
              Asistente de invocación
            </ActionButton>
          </div>
        </div>
      </section>
    </div>

    <section class="history-card card">
      <div class="section-title">
        <div>
          <p class="eyebrow">Historial</p>
          <h2>Últimas usadas y más usadas</h2>
        </div>
        <div class="inline-meta">
          <span class="meta-pill">Activas: {{ store.activeInstanceCount }}</span>
          <span class="meta-pill">Grupos: {{ store.groups.length }}</span>
        </div>
      </div>

      <div class="dual-grid">
        <div>
          <h3>Últimas usadas</h3>
          <div class="chip-list">
            <button
              v-for="shortcut in store.recentlyUsedSummons"
              :key="shortcut.id"
              class="chip-button"
              type="button"
              :disabled="store.busy"
              @click="handleShortcutSummon(shortcut.id, 'RECENT')"
            >
              {{ shortcut.displayName }}
            </button>
            <p v-if="store.recentlyUsedSummons.length === 0" class="muted">Todavía no hay accesos recientes.</p>
          </div>
        </div>

        <div>
          <h3>Más usadas</h3>
          <div class="chip-list">
            <button
              v-for="shortcut in store.mostUsedSummons"
              :key="shortcut.id"
              class="chip-button"
              type="button"
              :disabled="store.busy"
              @click="handleShortcutSummon(shortcut.id, 'MOST_USED')"
            >
              {{ shortcut.displayName }} · {{ shortcut.usageCount }}
            </button>
            <p v-if="store.mostUsedSummons.length === 0" class="muted">Todavía no hay accesos populares.</p>
          </div>
        </div>
      </div>
    </section>

    <teleport to="body">
      <SummonAssistantModal
        :open="isSummonAssistantOpen"
        :max-summon-monster-level="store.configuration.maxSummonMonsterLevel"
        @cancel="closeSummonAssistant"
        @invoke="handleAssistantSummon"
      />

      <div v-if="summonToasts.length > 0" class="toast-stack" aria-live="polite" aria-relevant="additions removals">
        <div v-for="toast in summonToasts" :key="toast.id" class="toast" role="status">
          {{ toast.message }}
        </div>
      </div>
    </teleport>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import DailyUsesPanel from '@/components/DailyUsesPanel.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import SummonAssistantModal from '@/components/SummonAssistantModal.vue';
import type { SummonAssistantChoiceNv3 } from '@/data/summonAssistantChoicesNv3';
import type { SummonAssistantChoiceNv4 } from '@/data/summonAssistantChoicesNv4';
import type { SummonAssistantChoiceNv5 } from '@/data/summonAssistantChoicesNv5';
import type { SummonAssistantChoiceNv6 } from '@/data/summonAssistantChoicesNv6';
import type { SummonAssistantChoiceNv7 } from '@/data/summonAssistantChoicesNv7';
import type { SummonAssistantChoiceNv8 } from '@/data/summonAssistantChoicesNv8';
import type { SummonAssistantChoiceNv9 } from '@/data/summonAssistantChoicesNv9';
import { useCombatStore } from '@/stores/combat';
import type { SummonTemplateType } from '@/types/catalog';

const store = useCombatStore();
const isSummonAssistantOpen = ref(false);
const summonToasts = ref<Array<{ id: number; message: string }>>([]);
const summonToastTimers = new Map<number, number>();
let summonToastSequence = 0;
const summonLevels = [1, 2, 3, 4, 5, 6, 7, 8, 9];
const MAX_SUMMON_TOASTS = 5;
const SUMMON_TOAST_DURATION_MS = 15_000;

const allowedTemplates = computed(() => store.selectedCreatureAllowedTemplates);
const templateSelectionRequired = computed(() => allowedTemplates.value.length > 1);
const canSummon = computed(() => {
  if (!store.selectedCreature) {
    return false;
  }

  return !templateSelectionRequired.value || store.selectedTemplate !== null;
});

function templateLabel(template: SummonTemplateType): string {
  const labels: Record<SummonTemplateType, string> = {
    CHTHONIC: 'Chthonic',
    FIERY: 'Fiery',
    CELESTIAL: 'Celestial',
    ENTROPIC: 'Entropic',
    RESOLUTE: 'Resolute',
  };

  return labels[template];
}

function showSummonToast(): void {
  const result = store.lastRollResult;
  if (result?.type !== 'SUMMON_QUANTITY') {
    return;
  }

  const contentParts = result.content.split('=');
  const quantityText = contentParts[contentParts.length - 1]?.trim();
  const title = result.title ?? 'Invocación completada';

  if (!quantityText) {
    return;
  }

  const message = `${title.replace('Cantidad invocada: ', '')}: ${quantityText} criatura(s).`;
  const id = ++summonToastSequence;
  const nextToasts = [...summonToasts.value, { id, message }];
  const overflow = nextToasts.length - MAX_SUMMON_TOASTS;

  if (overflow > 0) {
    const removedToasts = nextToasts.splice(0, overflow);
    for (const removedToast of removedToasts) {
      const removedTimer = summonToastTimers.get(removedToast.id);
      if (removedTimer) {
        window.clearTimeout(removedTimer);
        summonToastTimers.delete(removedToast.id);
      }
    }
  }

  summonToasts.value = nextToasts;

  const timer = window.setTimeout(() => {
    summonToasts.value = summonToasts.value.filter(toast => toast.id !== id);
    summonToastTimers.delete(id);
  }, SUMMON_TOAST_DURATION_MS);

  summonToastTimers.set(id, timer);
}

function onCreatureChange(event: Event): void {
  const target = event.target as HTMLSelectElement;
  store.selectCreature(target.value);
}

function onLevelFilterChange(event: Event): void {
  const target = event.target as HTMLSelectElement;
  const value = target.value ? Number(target.value) : null;
  store.selectCatalogLevel(value);
}

function onTemplateChange(event: Event): void {
  const target = event.target as HTMLSelectElement;
  store.selectTemplate(target.value ? (target.value as SummonTemplateType) : null);
}

function openSummonAssistant(): void {
  isSummonAssistantOpen.value = true;
}

function closeSummonAssistant(): void {
  isSummonAssistantOpen.value = false;
}

async function handleSummon(): Promise<void> {
  await store.summonSelectedCreature();
  showSummonToast();
}

async function handleAssistantSummon(suggestion: SummonAssistantChoiceNv3 | SummonAssistantChoiceNv4 | SummonAssistantChoiceNv5 | SummonAssistantChoiceNv6 | SummonAssistantChoiceNv7 | SummonAssistantChoiceNv8 | SummonAssistantChoiceNv9): Promise<void> {
  closeSummonAssistant();
  await store.summonCreatureById(suggestion.creatureId, suggestion.template);
  showSummonToast();
}

async function handleShortcutSummon(shortcutId: string, source: 'RECENT' | 'MOST_USED'): Promise<void> {
  const shortcut = source === 'RECENT'
    ? store.recentlyUsedSummons.find(item => item.id === shortcutId)
    : store.mostUsedSummons.find(item => item.id === shortcutId);

  if (!shortcut) {
    return;
  }

  await store.summonFromShortcut(shortcut, source);
  showSummonToast();
}

onMounted(() => {
  void store.initialize();
});

onBeforeUnmount(() => {
  for (const timer of summonToastTimers.values()) {
    window.clearTimeout(timer);
  }
  summonToastTimers.clear();
});
</script>

<style scoped>
.invocations-view {
  display: grid;
  gap: 1rem;
}

.error-banner {
  padding: 0.9rem 1rem;
  border-radius: 0.9rem;
  background: rgba(127, 29, 29, 0.65);
  border: 1px solid rgba(239, 68, 68, 0.35);
  color: #fecaca;
}

.top-grid {
  display: grid;
  grid-template-columns: 0.8fr 1.2fr;
  gap: 1rem;
}

.card {
  padding: 1rem;
  border-radius: 1rem;
  border: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(15, 23, 42, 0.72);
}

.summon-panel {
  display: grid;
  gap: 1rem;
}

.section-title {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: start;
}

.eyebrow,
.section-title h2,
.section-title h3,
p {
  margin: 0;
}

.toolbar {
  display: grid;
  grid-template-columns: auto minmax(0, 1.35fr) minmax(0, 1.1fr) auto;
  gap: 0.75rem;
  align-items: end;
}

.level-filter {
  max-width: 5.5rem;
}

.level-filter__select {
  min-width: 5.5rem;
  width: 100%;
}

.field-label {
  display: block;
  margin-bottom: 0.35rem;
  color: #cbd5e1;
  font-size: 0.82rem;
}

.button-stack {
  display: grid;
  gap: 0.5rem;
}

.history-section {
  display: grid;
  gap: 1rem;
}

.inline-meta {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.meta-pill {
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  background: rgba(30, 41, 59, 0.75);
  border: 1px solid rgba(148, 163, 184, 0.15);
  color: #cbd5e1;
  font-size: 0.82rem;
}

.muted {
  color: #94a3b8;
}

.dual-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.chip-button {
  min-height: 2.5rem;
  padding: 0.65rem 0.9rem;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(30, 41, 59, 0.6);
  color: inherit;
}

.toast {
  width: min(90vw, 360px);
  padding: 0.85rem 1rem;
  border-radius: 0.9rem;
  background: rgba(15, 23, 42, 0.96);
  border: 1px solid rgba(34, 197, 94, 0.35);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
  color: #dcfce7;
}

.toast-stack {
  position: fixed;
  right: 1rem;
  bottom: 1rem;
  z-index: 70;
  display: grid;
  gap: 0.5rem;
  justify-items: end;
}

@media (max-width: 980px) {
  .top-grid,
  .toolbar,
  .dual-grid {
    grid-template-columns: 1fr;
  }

  .inline-meta {
    justify-content: flex-start;
  }
}
</style>
