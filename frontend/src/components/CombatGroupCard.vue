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
      <div>
        <span>CA</span>
        <strong>{{ group.resolvedCreature.armorClass.normal }}</strong>
      </div>
      <div>
        <span>PG</span>
        <strong>{{ group.resolvedCreature.maxHitPoints }}</strong>
      </div>
      <div>
        <span>TS</span>
        <strong>Fort {{ group.resolvedCreature.savingThrows.fortitude }} / Ref {{ group.resolvedCreature.savingThrows.reflex }} / Vol {{ group.resolvedCreature.savingThrows.will }}</strong>
      </div>
      <div>
        <span>Ataques</span>
        <strong>{{ group.resolvedCreature.attacksText }}</strong>
      </div>
    </div>

    <div class="details">
      <p><strong>Alineación:</strong> {{ group.resolvedCreature.alignment }}</p>
      <p><strong>Tamaño:</strong> {{ group.resolvedCreature.size }}</p>
      <p><strong>Tipo:</strong> {{ group.resolvedCreature.creatureType }}</p>
      <p><strong>Velocidades:</strong> {{ group.resolvedCreature.speedsText }}</p>
      <p><strong>Ataques especiales:</strong> {{ specialAttacks }}</p>
    </div>

    <div class="button-row">
      <ActionButton :disabled="true">Atacar con todas</ActionButton>
      <ActionButton :disabled="true">Tirar TS</ActionButton>
      <ActionButton :disabled="busy" @click="$emit('expand')">Expandir ficha</ActionButton>
    </div>

    <div class="divider"></div>

    <div class="stack">
      <CreatureCard
        v-for="instance in group.instances"
        :key="instance.id"
        :instance="instance"
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

const props = defineProps<{
  group: ActiveSummonGroup;
  busy?: boolean;
}>();

defineEmits<{
  (event: 'damage', instanceId: string, amount: number): void;
  (event: 'heal', instanceId: string, amount: number): void;
  (event: 'remove', instanceId: string): void;
  (event: 'expand'): void;
}>();

const title = computed(() => props.group.resolvedCreature.displayName);
const specialAttacks = computed(() => {
  if (!props.group.resolvedCreature.specialAttacks.length) {
    return '—';
  }

  return props.group.resolvedCreature.specialAttacks.join(' · ');
});
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
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.summary-grid div {
  padding: 0.75rem;
  border-radius: 0.85rem;
  background: rgba(30, 41, 59, 0.7);
}

.summary-grid span {
  display: block;
  color: #94a3b8;
  font-size: 0.78rem;
}

.summary-grid strong {
  font-size: 0.96rem;
  word-break: break-word;
}

.details {
  display: grid;
  gap: 0.35rem;
  margin-bottom: 1rem;
  color: #cbd5e1;
}

.button-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 0.75rem;
}

.stack {
  display: grid;
  gap: 0.75rem;
}

@media (max-width: 720px) {
  .group-header {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
