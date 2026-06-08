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
          <label>
            <span class="field-label">Criatura</span>
            <select :value="store.selectedCreatureId ?? ''" @change="onCreatureChange">
              <option v-for="item in store.catalogItems" :key="item.id" :value="item.id">
                {{ item.name }} · nivel {{ item.summonLevel }}
              </option>
            </select>
          </label>

          <label>
            <span class="field-label">Plantilla</span>
            <select :value="store.selectedTemplate ?? ''" @change="onTemplateChange">
              <option :value="''">Sin plantilla</option>
              <option v-for="template in allowedTemplates" :key="template" :value="template">
                {{ templateLabel(template) }}
              </option>
            </select>
          </label>

          <div class="button-stack">
            <ActionButton :disabled="store.busy || !store.selectedCreature" @click="handleSummon">
              Invocar
            </ActionButton>
            <ActionButton :disabled="store.busy" variant="danger" @click="handleClearSummons">
              Limpiar invocaciones
            </ActionButton>
          </div>
        </div>

        <div class="mini-grid">
          <div class="meta-item">
            <span>Criaturas activas</span>
            <strong>{{ store.activeInstanceCount }}</strong>
          </div>
          <div class="meta-item">
            <span>Grupos activos</span>
            <strong>{{ store.groups.length }}</strong>
          </div>
          <div class="meta-item">
            <span>Último resultado</span>
            <strong>{{ store.lastRollResult?.title ?? '—' }}</strong>
          </div>
          <div class="meta-item">
            <span>Plantillas permitidas</span>
            <strong>{{ allowedTemplates.map(templateLabel).join(' · ') || '—' }}</strong>
          </div>
        </div>

        <div v-if="store.lastRollResult" class="last-roll">
          <p class="eyebrow">Resultado más reciente</p>
          <strong>{{ store.lastRollResult.title }}</strong>
          <p class="muted">{{ store.lastRollResult.content }}</p>
        </div>
      </section>
    </div>

    <section class="card">
      <div class="section-title">
        <div>
          <p class="eyebrow">Historial</p>
          <h2>Últimas usadas y más usadas</h2>
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

    <section class="card">
      <div class="section-title">
        <div>
          <p class="eyebrow">Grupos activos</p>
          <h2>Invocaciones en combate</h2>
        </div>
      </div>

      <p v-if="store.groups.length === 0" class="empty">No hay invocaciones activas.</p>

      <div v-else class="groups-list">
        <CombatGroupCard
          v-for="group in store.groups"
          :key="group.id"
          :group="group"
          :busy="store.busy"
          @damage="handleDamage"
          @heal="handleHeal"
          @remove="handleRemove"
          @expand="expandedGroupId = group.id"
        />
      </div>
    </section>

    <teleport to="body">
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
            <div><strong>Id:</strong> {{ expandedGroup.resolvedCreature.id }}</div>
            <div><strong>Alineación:</strong> {{ expandedGroup.resolvedCreature.alignment }}</div>
            <div><strong>Tamaño:</strong> {{ expandedGroup.resolvedCreature.size }}</div>
            <div><strong>Tipo:</strong> {{ expandedGroup.resolvedCreature.creatureType }}</div>
          </div>

          <pre class="statblock">{{ expandedGroup.resolvedCreature.fullStatBlock }}</pre>
        </section>
      </div>
    </teleport>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import CombatGroupCard from '@/components/CombatGroupCard.vue';
import DailyUsesPanel from '@/components/DailyUsesPanel.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import { useCombatStore } from '@/stores/combat';
import type { SummonTemplateType } from '@/types/catalog';

const store = useCombatStore();
const expandedGroupId = ref<string | null>(null);

const allowedTemplates = computed(() => store.selectedCreatureAllowedTemplates);
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

function onCreatureChange(event: Event): void {
  const target = event.target as HTMLSelectElement;
  store.selectCreature(target.value);
}

function onTemplateChange(event: Event): void {
  const target = event.target as HTMLSelectElement;
  store.selectTemplate(target.value ? (target.value as SummonTemplateType) : null);
}

async function handleSummon(): Promise<void> {
  await store.summonSelectedCreature();
}

async function handleShortcutSummon(shortcutId: string, source: 'RECENT' | 'MOST_USED'): Promise<void> {
  const shortcut = source === 'RECENT'
    ? store.recentlyUsedSummons.find(item => item.id === shortcutId)
    : store.mostUsedSummons.find(item => item.id === shortcutId);

  if (!shortcut) {
    return;
  }

  await store.summonFromShortcut(shortcut, source);
}

async function handleClearSummons(): Promise<void> {
  await store.clearSummons();
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

onMounted(() => {
  void store.initialize();
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
  grid-template-columns: 1fr 1fr auto;
  gap: 0.75rem;
  align-items: end;
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

.mini-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
}

.meta-item {
  padding: 0.85rem;
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(51, 65, 85, 0.8);
}

.meta-item span {
  display: block;
  color: #94a3b8;
  font-size: 0.85rem;
}

.meta-item strong {
  display: block;
  margin-top: 0.25rem;
  font-size: 1rem;
}

.last-roll {
  padding: 0.85rem;
  border-radius: 0.85rem;
  background: rgba(2, 6, 23, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
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

@media (max-width: 980px) {
  .top-grid,
  .toolbar,
  .dual-grid,
  .expanded-meta {
    grid-template-columns: 1fr;
  }
}
</style>
