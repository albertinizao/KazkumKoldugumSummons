<template>
  <section class="panel">
    <div class="panel-header">
      <h2>Usos diarios</h2>
      <StatusBadge variant="neutral">{{ state.remaining }} / {{ state.maximum }}</StatusBadge>
    </div>

    <div class="button-row">
      <ActionButton>Invocar</ActionButton>
      <ActionButton>Limpiar invocaciones</ActionButton>
    </div>

    <p class="hint">
      Esqueleto inicial del frontend. Aquí se conectará el estado de combate, el catálogo y las
      acciones de mesa.
    </p>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import { useCombatStore } from '@/stores/combat';

const store = useCombatStore();
const state = computed(() => store.dailyUses);
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
