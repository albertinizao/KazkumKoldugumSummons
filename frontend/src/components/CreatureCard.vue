<template>
  <article class="creature-card" :class="`creature-card--${instance.status.toLowerCase()}`">
    <div class="creature-card__header">
      <div>
        <strong>{{ instance.displayName }}</strong>
        <p class="muted">Identificador: {{ instance.id }}</p>
      </div>
      <StatusBadge :variant="statusVariant">{{ instance.status }}</StatusBadge>
    </div>

    <p class="hp-line">PG: {{ instance.currentHitPoints }} / {{ instance.maxHitPoints }}</p>

    <div class="button-row">
      <ActionButton :disabled="busy" @click="$emit('damage', 1)">Dañar</ActionButton>
      <ActionButton :disabled="busy" @click="$emit('heal', 1)">Curar</ActionButton>
      <ActionButton :disabled="busy" variant="danger" @click="$emit('remove')">Eliminar</ActionButton>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import type { ActiveSummonInstance } from '@/types/combat';

const props = defineProps<{
  instance: ActiveSummonInstance;
  busy?: boolean;
}>();

defineEmits<{
  (event: 'damage', amount: number): void;
  (event: 'heal', amount: number): void;
  (event: 'remove'): void;
}>();

const statusVariant = computed(() => {
  if (props.instance.status === 'HEALTHY') {
    return 'success';
  }

  if (props.instance.status === 'DOWN') {
    return 'danger';
  }

  return 'warning';
});
</script>

<style scoped>
.creature-card {
  padding: 0.9rem;
  border-radius: 0.9rem;
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.creature-card--down {
  outline: 1px solid rgba(239, 68, 68, 0.25);
}

.creature-card__header {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: start;
}

.hp-line,
.muted {
  margin: 0.45rem 0 0;
  color: #cbd5e1;
}

.muted {
  font-size: 0.82rem;
  color: #94a3b8;
}

.button-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.5rem;
  margin-top: 0.75rem;
}
</style>
