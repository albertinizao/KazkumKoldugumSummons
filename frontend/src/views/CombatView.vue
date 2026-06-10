<template>
  <section class="combat-view">
    <div v-if="store.error" class="error-banner">{{ store.error }}</div>

    <div class="top-grid">
      <DailyUsesPanel />

      <section class="summon-panel card">
        <div class="section-title">
          <div>
            <p class="eyebrow">Invocación</p>
            <h2>Seleccionar criatura</h2>
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
            <ActionButton :disabled="store.busy" variant="danger" @click="handleClearSummons">
              Limpiar
            </ActionButton>
          </div>
        </div>

        <section class="history-section">
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

      </section>
    </div>

    <section class="card">
      <div class="section-title">
        <div>
          <h2>Invocaciones en combate</h2>
        </div>
      </div>

      <div class="global-actions combat-global-actions">
        <ActionButton :disabled="store.busy || store.groups.length === 0" @click="handleRollAllAttacks">
          Atacar con todas
        </ActionButton>
        <ActionButton :disabled="store.busy || store.groups.length === 0" @click="handleRollAllSavingThrows">
          Tirar TS con todas
        </ActionButton>
      </div>

      <p v-if="store.groups.length === 0" class="empty">No hay invocaciones activas.</p>

      <div v-else class="groups-list">
        <CombatGroupCard
          v-for="group in store.groups"
          :key="group.id"
          :group="group"
          :busy="store.busy"
          @attack="handleAttack(group.id)"
          @saving-throws="handleSavingThrows(group.id)"
          @damage="handleDamage"
          @heal="handleHeal"
          @remove="handleRemove"
          @expand="expandedGroupId = group.id"
        />
      </div>
    </section>

    <teleport to="body">
      <SummonAssistantModal
        :open="isSummonAssistantOpen"
        :max-summon-monster-level="store.configuration.maxSummonMonsterLevel"
        @cancel="closeSummonAssistant"
        @invoke="handleAssistantSummon"
      />

      <div v-if="isRollResultModalOpen && store.lastCombatRollResult" class="modal-backdrop" @click.self="closeRollResultModal">
        <section class="modal roll-modal" role="dialog" aria-modal="true" aria-labelledby="roll-result-title">
          <div class="modal-header">
            <div>
              <p class="eyebrow">Resultado de tirada</p>
              <h2 id="roll-result-title">{{ store.lastCombatRollResult.title }}</h2>
            </div>
            <ActionButton @click="closeRollResultModal">Cerrar</ActionButton>
          </div>

          <CombatRollResultPanel :result="store.lastCombatRollResult" />
        </section>
      </div>

      <div v-if="globalRollResult" class="modal-backdrop" @click.self="closeGlobalRollResultModal">
        <section class="modal roll-modal" role="dialog" aria-modal="true" :aria-labelledby="globalRollResultTitleId">
          <div class="modal-header">
            <div>
              <p class="eyebrow">Resultado global</p>
              <h2 :id="globalRollResultTitleId">{{ globalRollResult.title }}</h2>
            </div>
            <ActionButton @click="closeGlobalRollResultModal">Cerrar</ActionButton>
          </div>

          <GlobalCombatRollResultPanel :result="globalRollResult" />
        </section>
      </div>

      <div v-if="expandedGroup" class="modal-backdrop" @click.self="expandedGroupId = null">
        <section class="modal expanded-modal" role="dialog" aria-modal="true" aria-labelledby="expanded-title">
          <div class="modal-header">
            <div>
              <p class="eyebrow">Ficha expandida</p>
              <h2 id="expanded-title">{{ expandedGroup.resolvedCreature.displayName }}</h2>
            </div>
            <ActionButton @click="expandedGroupId = null">Cerrar</ActionButton>
          </div>

          <div class="expanded-meta">
            <div><strong>alineamiento:</strong> {{ expandedGroup.resolvedCreature.alignment }}</div>
            <div><strong>Tamaño:</strong> {{ expandedGroup.resolvedCreature.size }}</div>
            <div><strong>Tipo:</strong> {{ creatureTypeLabel(expandedGroup.resolvedCreature) }}</div>
            <div><strong>Iniciativa:</strong> {{ expandedGroup.resolvedCreature.initiative }}</div>
            <div><strong>Sentidos:</strong> {{ joinOrDash(expandedGroup.resolvedCreature.senses) }}</div>
            <div><strong>Percepción:</strong> {{ expandedGroup.resolvedCreature.perception }}</div>
            <div><strong>CA:</strong> {{ expandedGroup.resolvedCreature.armorClass.normal }}</div>
            <div><strong>CA toque:</strong> {{ expandedGroup.resolvedCreature.armorClass.touch }}</div>
            <div><strong>CA desprevenida:</strong> {{ expandedGroup.resolvedCreature.armorClass.flatFooted }}</div>
            <div><strong>PG:</strong> {{ expandedGroup.resolvedCreature.maxHitPoints }}</div>
            <div><strong>Fortaleza:</strong> {{ expandedGroup.resolvedCreature.savingThrows.fortitude }}</div>
            <div><strong>Reflejos:</strong> {{ expandedGroup.resolvedCreature.savingThrows.reflex }}</div>
            <div><strong>Voluntad:</strong> {{ expandedGroup.resolvedCreature.savingThrows.will }}</div>
            <div><strong>Velocidades:</strong> {{ joinOrDashTexts(expandedGroup.resolvedCreature.speedsText) }}</div>
            <div><strong>Ataques básicos:</strong> {{ joinOrDashTexts(expandedGroup.resolvedCreature.attacksText) }}</div>
            <div><strong>Espacio:</strong> {{ expandedGroup.resolvedCreature.space }}</div>
            <div><strong>Alcance:</strong> {{ expandedGroup.resolvedCreature.reach }}</div>
            <div><strong>Ataques especiales:</strong> {{ joinOrDash(expandedGroup.resolvedCreature.specialAttacks) }}</div>
            <div><strong>Inmunidades / SR:</strong> {{ formatSpecialDefenseList(expandedGroup.resolvedCreature.specialDefenses, ['IMMUNITY', 'SPELL_RESISTANCE'], true) }}</div>
            <div><strong>Vulnerabilidades:</strong> {{ formatSpecialDefenseList(expandedGroup.resolvedCreature.specialDefenses, ['VULNERABILITY']) }}</div>
            <div><strong>RD / resistencias:</strong> {{ formatSpecialDefenseList(expandedGroup.resolvedCreature.specialDefenses, ['DAMAGE_REDUCTION', 'RESISTANCE']) }}</div>
            <div><strong>Defensas especiales:</strong> {{ joinOrDash(expandedGroup.resolvedCreature.specialDefenses.map(defense => formatSpecialDefense(defense))) }}</div>
            <div><strong>Reglas aplicadas:</strong> {{ joinOrDash(expandedGroup.resolvedCreature.appliedRules.map(rule => rule.description)) }}</div>
            <div><strong>Aptitudes resumidas:</strong> {{ joinOrDash(expandedGroup.resolvedCreature.shortAbilities.map(ability => `${ability.name}: ${ability.summary}`)) }}</div>
            <div><strong>Aptitudes completas:</strong> {{ joinOrDash(expandedGroup.resolvedCreature.expandedAbilities.map(ability => `${ability.name}: ${ability.text}`)) }}</div>
          </div>

          <pre class="statblock">{{ expandedGroup.resolvedCreature.fullStatBlock }}</pre>
        </section>
      </div>

      <div v-if="summonToast" class="toast" role="status" aria-live="polite">
        {{ summonToast }}
      </div>
    </teleport>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import CombatGroupCard from '@/components/CombatGroupCard.vue';
import CombatRollResultPanel from '@/components/CombatRollResultPanel.vue';
import GlobalCombatRollResultPanel from '@/components/GlobalCombatRollResultPanel.vue';
import DailyUsesPanel from '@/components/DailyUsesPanel.vue';
import SummonAssistantModal from '@/components/SummonAssistantModal.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import { useCombatStore } from '@/stores/combat';
import type { SummonTemplateType } from '@/types/catalog';
import type { GlobalCombatRollResult } from '@/types/combat';
import type { SummonAssistantSuggestion } from '@/data/summonAssistant';
import { formatCreatureTypeWithSubtypes } from '@/utils/creatureDisplay';
import { formatSpecialDefense, formatSpecialDefenseList } from '@/utils/specialDefenseDisplay';

const store = useCombatStore();
const expandedGroupId = ref<string | null>(null);
const isSummonAssistantOpen = ref(false);
const isRollResultModalOpen = ref(false);
const globalRollResult = ref<GlobalCombatRollResult | null>(null);
const summonToast = ref<string | null>(null);
let summonToastTimer: number | undefined;
const globalRollResultTitleId = 'global-roll-result-title';
const summonLevels = [1, 2, 3, 4, 5, 6, 7, 8, 9];

const allowedTemplates = computed(() => store.selectedCreatureAllowedTemplates);
const templateSelectionRequired = computed(() => allowedTemplates.value.length > 1);
const canSummon = computed(() => {
  if (!store.selectedCreature) {
    return false;
  }

  return !templateSelectionRequired.value || store.selectedTemplate !== null;
});
const expandedGroup = computed(() => store.groups.find(group => group.id === expandedGroupId.value) ?? null);

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

function creatureTypeLabel(creature: { creatureType: string; subtypes: string[] }): string {
  return formatCreatureTypeWithSubtypes(creature.creatureType, creature.subtypes);
}

function joinOrDash(values: string[]): string {
  return values.length === 0 ? '—' : values.join(' · ');
}

function joinOrDashTexts(value: string): string {
  return value && value.trim() ? value : '—';
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

  summonToast.value = `${title.replace('Cantidad invocada: ', '')}: ${quantityText} criatura(s).`;

  if (summonToastTimer) {
    window.clearTimeout(summonToastTimer);
  }

  summonToastTimer = window.setTimeout(() => {
    summonToast.value = null;
  }, 2500);
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
  summonToast.value = null;
  await store.summonSelectedCreature();
  showSummonToast();
}

async function handleAssistantSummon(suggestion: SummonAssistantSuggestion): Promise<void> {
  summonToast.value = null;
  closeSummonAssistant();
  await store.summonCreatureById(suggestion.variant.creatureId, suggestion.variant.template);
  showSummonToast();
}

async function handleShortcutSummon(shortcutId: string, source: 'RECENT' | 'MOST_USED'): Promise<void> {
  const shortcut = source === 'RECENT'
    ? store.recentlyUsedSummons.find(item => item.id === shortcutId)
    : store.mostUsedSummons.find(item => item.id === shortcutId);

  if (!shortcut) {
    return;
  }

  summonToast.value = null;
  await store.summonFromShortcut(shortcut, source);
  showSummonToast();
}

async function handleClearSummons(): Promise<void> {
  await store.clearSummons();
  isRollResultModalOpen.value = false;
  globalRollResult.value = null;
  summonToast.value = null;
}

async function handleAttack(groupId: string): Promise<void> {
  await store.rollGroupAttacks(groupId);
  if (store.lastCombatRollResult) {
    isRollResultModalOpen.value = true;
  }
}

async function handleSavingThrows(groupId: string): Promise<void> {
  await store.rollGroupSavingThrows(groupId);
  if (store.lastCombatRollResult) {
    isRollResultModalOpen.value = true;
  }
}

async function handleRollAllAttacks(): Promise<void> {
  globalRollResult.value = null;
  globalRollResult.value = await store.rollAllGroupAttacks();
}

async function handleRollAllSavingThrows(): Promise<void> {
  globalRollResult.value = null;
  globalRollResult.value = await store.rollAllGroupSavingThrows();
}

async function handleDamage(instanceId: string, amount: number): Promise<void> {
  await store.damageCreature(instanceId, amount);
}

async function handleHeal(instanceId: string, amount: number): Promise<void> {
  await store.healCreature(instanceId, amount);
}

async function handleRemove(instanceId: string): Promise<void> {
  await store.removeCreature(instanceId);
}

function closeRollResultModal(): void {
  isRollResultModalOpen.value = false;
}

function closeGlobalRollResultModal(): void {
  globalRollResult.value = null;
}

onMounted(() => {
  void store.initialize();
});

onBeforeUnmount(() => {
  if (summonToastTimer) {
    window.clearTimeout(summonToastTimer);
  }
});
</script>

<style scoped>
.combat-view {
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

.global-actions {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
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

.groups-list {
  display: grid;
  gap: 1rem;
}

.empty {
  color: #94a3b8;
}

.modal {
  width: min(900px, 100%);
  max-height: min(90vh, 920px);
  overflow: auto;
  background: rgba(15, 23, 42, 0.96);
  border: 1px solid rgba(51, 65, 85, 0.92);
  border-radius: 28px;
  box-shadow: 0 28px 60px rgba(0, 0, 0, 0.42);
  padding: 1rem;
}

.expanded-modal {
  display: grid;
  gap: 1rem;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: start;
}

.expanded-meta {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
}

.expanded-meta > div {
  padding: 0.75rem;
  border-radius: 0.85rem;
  background: rgba(30, 41, 59, 0.6);
}

.statblock {
  margin: 0;
  padding: 0.9rem;
  border-radius: 0.85rem;
  background: rgba(2, 6, 23, 0.4);
  border: 1px solid rgba(148, 163, 184, 0.14);
  white-space: pre-wrap;
  word-break: break-word;
}

.toast {
  position: fixed;
  right: 1rem;
  bottom: 1rem;
  z-index: 70;
  padding: 0.85rem 1rem;
  border-radius: 0.9rem;
  background: rgba(15, 23, 42, 0.96);
  border: 1px solid rgba(34, 197, 94, 0.35);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
  color: #dcfce7;
  max-width: min(90vw, 360px);
}

@media (max-width: 980px) {
  .top-grid,
  .toolbar,
  .dual-grid,
  .expanded-meta {
    grid-template-columns: 1fr;
  }

  .inline-meta {
    justify-content: flex-start;
  }
}
</style>
