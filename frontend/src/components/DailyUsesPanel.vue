<template>
  <section class="panel">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Combate</p>
        <h2>Usos diarios</h2>
      </div>
      <StatusBadge variant="neutral">{{ state.remaining }} / {{ state.maximum }}</StatusBadge>
    </div>

    <div class="button-row">
      <ActionButton :disabled="busy" @click="$emit('summon')">Invocar</ActionButton>
      <ActionButton :disabled="busy" variant="danger" @click="$emit('clearSummons')">Limpiar invocaciones</ActionButton>
    </div>

    <p class="hint">
      El contador se conserva al recargar. Invocar descuenta 1 uso si hay margen, pero no bloquea la mesa si ya está a 0.
    </p>
  </section>
</template>

<script setup lang="ts">
import ActionButton from '@/components/ActionButton.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import type { DailyUses } from '@/types/combat';

defineProps<{
  state: DailyUses;
  busy?: boolean;
}>();

defineEmits<{
  (event: 'summon'): void;
  (event: 'clearSummons'): void;
}>();
</script>

<style scoped>
.panel {
  padding: 1rem;
  border-radius: 1rem;
  border: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(15, 23, 42, 0.72);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.eyebrow,
h2 {
  margin: 0;
}

.button-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 0.75rem;
}

.hint {
  margin: 1rem 0 0;
  color: #94a3b8;
}
</style>
