<template>
  <section class="combat-view">
    <div v-if="store.error" class="error-banner">{{ store.error }}</div>

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
    </teleport>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import CombatGroupCard from '@/components/CombatGroupCard.vue';
import CombatRollResultPanel from '@/components/CombatRollResultPanel.vue';
import GlobalCombatRollResultPanel from '@/components/GlobalCombatRollResultPanel.vue';
import { useCombatStore } from '@/stores/combat';
import type { GlobalCombatRollResult } from '@/types/combat';
import { formatCreatureTypeWithSubtypes } from '@/utils/creatureDisplay';
import { formatSpecialDefense, formatSpecialDefenseList } from '@/utils/specialDefenseDisplay';

const store = useCombatStore();
const expandedGroupId = ref<string | null>(null);
const isRollResultModalOpen = ref(false);
const globalRollResult = ref<GlobalCombatRollResult | null>(null);
const globalRollResultTitleId = 'global-roll-result-title';
const expandedGroup = computed(() => store.groups.find(group => group.id === expandedGroupId.value) ?? null);

function creatureTypeLabel(creature: { creatureType: string; subtypes: string[] }): string {
  return formatCreatureTypeWithSubtypes(creature.creatureType, creature.subtypes);
}

function joinOrDash(values: string[]): string {
  return values.length === 0 ? '—' : values.join(' · ');
}

function joinOrDashTexts(value: string): string {
  return value && value.trim() ? value : '—';
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

.card {
  padding: 1rem;
  border-radius: 1rem;
  border: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(15, 23, 42, 0.72);
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

.global-actions {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.muted {
  color: #94a3b8;
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
  .expanded-meta {
    grid-template-columns: 1fr;
  }
}
</style>
