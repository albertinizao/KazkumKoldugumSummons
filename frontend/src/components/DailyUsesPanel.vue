<template>
  <section class="panel">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Recursos</p>
        <h2>Usos diarios</h2>
      </div>
      <StatusBadge variant="neutral">{{ state.remaining }} / {{ state.maximum }}</StatusBadge>
    </div>

    <div class="controls">
      <label class="amount-field">
        <span class="small muted">Cantidad</span>
        <input v-model.number="amount" type="number" min="1" step="1" inputmode="numeric" />
      </label>

      <div class="button-row">
        <ActionButton :disabled="isBusy || !isAmountValid" @click="increment">+{{ amount }}</ActionButton>
        <ActionButton :disabled="isBusy || !isAmountValid" @click="decrement">-{{ amount }}</ActionButton>
        <ActionButton :disabled="isBusy" @click="resetUses">Resetear</ActionButton>
      </div>
    </div>

    <p class="hint">
      La invocación nunca se bloquea rígidamente a 0. El contador se mantiene dentro de sus límites y se sincroniza con el backend.
    </p>

    <p v-if="store.dailyUsesError" class="error">
      {{ store.dailyUsesError }}
    </p>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import { useCombatStore } from '@/stores/combat';

const store = useCombatStore();
const amount = ref(1);

const state = computed(() => store.dailyUses);
const isBusy = computed(() => store.dailyUsesLoading);
const isAmountValid = computed(() => Number.isInteger(amount.value) && amount.value >= 1);

onMounted(() => {
  void store.loadDailyUses();
});

async function increment() {
  await store.increaseDailyUses(amount.value);
}

async function decrement() {
  await store.decreaseDailyUses(amount.value);
}

async function resetUses() {
  await store.resetDailyUses();
}
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
h2,
p {
  margin: 0;
}

.controls {
  display: grid;
  gap: 0.85rem;
}

.amount-field {
  display: grid;
  gap: 0.35rem;
}

.amount-field input {
  min-height: 3rem;
  border-radius: 0.85rem;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(30, 41, 59, 0.7);
  color: inherit;
  padding: 0 0.85rem;
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

.error {
  margin: 0.75rem 0 0;
  color: #fca5a5;
}
</style>
