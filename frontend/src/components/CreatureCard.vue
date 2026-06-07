<template>
  <article class="creature-card">
    <div class="creature-card__header">
      <strong>{{ label }}</strong>
      <StatusBadge :variant="statusVariant">{{ state }}</StatusBadge>
    </div>
    <p>PG: {{ currentHitPoints }} / {{ maxHitPoints }}</p>
    <div class="button-row">
      <ActionButton>Dañar</ActionButton>
      <ActionButton>Curar</ActionButton>
      <ActionButton>Eliminar</ActionButton>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import type { CreatureInstanceState } from '@/types/domain';

const props = defineProps<{
  label: string;
  currentHitPoints: number;
  maxHitPoints: number;
  state: CreatureInstanceState;
}>();

const statusVariant = computed(() => {
  if (props.state === 'HEALTHY') {
    return 'success';
  }

  if (props.state === 'DOWN') {
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

.creature-card__header {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: center;
}

p {
  margin: 0.5rem 0 0.75rem;
  color: #cbd5e1;
}

.button-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.5rem;
}
</style>
