<template>
  <article class="group-card">
    <header class="group-header">
      <div>
        <p class="eyebrow">Grupo activo</p>
        <h3>{{ title }}</h3>
        <p class="muted">{{ group.instances.length }} criatura(s) activas</p>
      </div>
      <StatusBadge variant="success">Grupo activo</StatusBadge>
    </header>

    <div class="summary-grid">
      <div class="summary-core">
        <div class="summary-card">
          <span>CA</span>
          <strong>{{ group.resolvedCreature.armorClass.normal }}</strong>
        </div>
        <div class="summary-card">
          <span>Fortaleza</span>
          <strong>{{ group.resolvedCreature.savingThrows.fortitude }}</strong>
        </div>
        <div class="summary-card">
          <span>Reflejos</span>
          <strong>{{ group.resolvedCreature.savingThrows.reflex }}</strong>
        </div>
        <div class="summary-card">
          <span>Voluntad</span>
          <strong>{{ group.resolvedCreature.savingThrows.will }}</strong>
        </div>
      </div>
      <div class="summary-attack">
        <div class="summary-card summary-card-attack">
          <span>Ataques</span>
          <strong>{{ group.resolvedCreature.attacksText }}</strong>
        </div>
      </div>
    </div>

    <div class="details-columns">
      <div class="details-column">
        <p><strong>Alineamiento:</strong> {{ group.resolvedCreature.alignment }}</p>
        <p><strong>Tamaño:</strong> {{ group.resolvedCreature.size }}</p>
        <p><strong>Tipo:</strong> {{ creatureTypeLabel }}</p>
        <p><strong>Velocidades:</strong> {{ group.resolvedCreature.speedsText }}</p>
        <p><strong>Ataques especiales:</strong> {{ specialAttacks }}</p>
        <p v-if="groupDefenses !== '—'"><strong>Inmunidades / SR:</strong> {{ groupDefenses }}</p>
        <p v-if="vulnerabilityDefenses !== '—'"><strong>Vulnerabilidades:</strong> {{ vulnerabilityDefenses }}</p>
      </div>
      <div class="details-column">
        <p><strong>PG:</strong> {{ hitPointsLabel }}</p>
        <p><strong>CA toque:</strong> {{ group.resolvedCreature.armorClass.touch }}</p>
        <p><strong>CA desprevenida:</strong> {{ group.resolvedCreature.armorClass.flatFooted }}</p>
        <p><strong>CMB:</strong> {{ group.resolvedCreature.cmb }}</p>
        <p><strong>CMD:</strong> {{ group.resolvedCreature.cmd }}</p>
      </div>
    </div>

    <div class="button-row">
      <ActionButton :disabled="busy || !hasAttacks" @click="$emit('attack')">Atacar</ActionButton>
      <ActionButton :disabled="busy || group.instances.length === 0" @click="$emit('saving-throws')">Salvaciones</ActionButton>
      <ActionButton :disabled="busy" @click="$emit('expand')">Expandir ficha</ActionButton>
    </div>

    <div class="divider"></div>

    <div class="stack">
      <CreatureCard
        class="creature-card"
        v-for="instance in group.instances"
        :key="instance.id"
        :instance="instance"
        :defense-summary="instanceDefenses"
        :immunity-summary="instanceImmunities"
        :vulnerability-summary="instanceVulnerabilities"
        :busy="busy"
        @damage="$emit('damage', instance.id, $event)"
        @heal="$emit('heal', instance.id, $event)"
        @remove="$emit('remove', instance.id)"
      />
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import CreatureCard from '@/components/CreatureCard.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import type { ActiveSummonGroup } from '@/types/combat';
import { formatCreatureTypeWithSubtypes } from '@/utils/creatureDisplay';
import { formatSpecialDefenseList } from '@/utils/specialDefenseDisplay';

const props = defineProps<{
  group: ActiveSummonGroup;
  busy?: boolean;
}>();

defineEmits<{
  (event: 'attack'): void;
  (event: 'saving-throws'): void;
  (event: 'damage', instanceId: string, amount: number): void;
  (event: 'heal', instanceId: string, amount: number): void;
  (event: 'remove', instanceId: string): void;
  (event: 'expand'): void;
}>();

const title = computed(() => props.group.resolvedCreature.displayName);
const creatureTypeLabel = computed(() =>
  formatCreatureTypeWithSubtypes(
    props.group.resolvedCreature.creatureType,
    props.group.resolvedCreature.subtypes,
  ),
);
const hasAttacks = computed(() => props.group.resolvedCreature.attacks.length > 0);
const hitPointsLabel = computed(() => props.group.resolvedCreature.hitPointsFormula?.trim() ?? `${props.group.resolvedCreature.maxHitPoints}`);
const specialAttacks = computed(() => {
  if (!props.group.resolvedCreature.specialAttacks.length) {
    return '—';
  }

  return props.group.resolvedCreature.specialAttacks.join(' · ');
});
const groupDefenses = computed(() =>
  formatSpecialDefenseList(props.group.resolvedCreature.specialDefenses, ['IMMUNITY', 'SPELL_RESISTANCE'], true),
);
const vulnerabilityDefenses = computed(() =>
  formatSpecialDefenseList(props.group.resolvedCreature.specialDefenses, ['VULNERABILITY']),
);
const instanceDefenses = computed(() =>
  formatSpecialDefenseList(props.group.resolvedCreature.specialDefenses, ['DAMAGE_REDUCTION', 'RESISTANCE']),
);
const instanceImmunities = computed(() =>
  formatSpecialDefenseList(props.group.resolvedCreature.specialDefenses, ['IMMUNITY'], true),
);
const instanceVulnerabilities = computed(() =>
  formatSpecialDefenseList(props.group.resolvedCreature.specialDefenses, ['VULNERABILITY']),
);
</script>

<style scoped>
.group-card {
  padding: 1rem;
  border-radius: 1rem;
  border: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(15, 23, 42, 0.72);
}

.group-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: start;
  margin-bottom: 1rem;
}

.eyebrow,
h3,
p {
  margin: 0;
}

.muted {
  color: #94a3b8;
}

.summary-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.summary-core {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
}

.summary-attack {
  min-width: 0;
}

.summary-card {
  padding: 0.75rem;
  border-radius: 0.85rem;
  background: rgba(30, 41, 59, 0.7);
  min-width: 0;
}

.summary-card span {
  display: block;
  color: #94a3b8;
  font-size: 0.78rem;
}

.summary-card strong {
  font-size: 1.05rem;
  word-break: break-word;
}

.summary-card-attack strong {
  display: block;
  white-space: normal;
}

.details-columns {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 0.85rem;
  margin-bottom: 1rem;
  color: #cbd5e1;
}

.details-column {
  display: grid;
  gap: 0.35rem;
}

.details strong {
  text-transform: none;
}

.button-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 0.75rem;
}

.stack {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 0.75rem;
}

.creature-card {
  min-width: 0;
}

@media (min-width: 421px) and (max-width: 1024px) {
  .summary-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 420px) {
  .group-header {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .summary-core {
    grid-template-columns: 1fr;
  }
}
</style>
