<template>
  <article class="creature-card" :class="`creature-card--${instance.status.toLowerCase()}`">
    <div class="creature-card__header">
      <div>
        <strong>{{ instance.displayName }}</strong>
      </div>
      <StatusBadge :variant="statusVariant">{{ instance.status }}</StatusBadge>
    </div>

    <p class="hp-line">PG: {{ instance.currentHitPoints }} / {{ instance.maxHitPoints }}</p>
    <p v-if="visibleDefenseSummary !== '—'" class="defense-line">{{ visibleDefenseSummary }}</p>

    <div class="quick-grid">
      <button class="mini-button danger" type="button" :disabled="busy" @click="applyDelta(-10)">-10</button>
      <button class="mini-button danger" type="button" :disabled="busy" @click="applyDelta(-5)">-5</button>
      <button class="mini-button danger" type="button" :disabled="busy" @click="applyDelta(-1)">-1</button>
      <button class="mini-button positive" type="button" :disabled="busy" @click="applyDelta(1)">+1</button>
      <button class="mini-button positive" type="button" :disabled="busy" @click="applyDelta(5)">+5</button>
      <button class="mini-button positive" type="button" :disabled="busy" @click="applyDelta(10)">+10</button>
    </div>

    <div class="custom-row">
      <label class="custom-input">
        <span class="muted small">Cantidad libre</span>
        <input v-model.number="customAmount" :disabled="busy" type="number" min="1" step="1" inputmode="numeric" placeholder="3" />
      </label>
      <div class="custom-actions">
        <ActionButton :disabled="busy || !isCustomAmountValid" variant="danger" @click="applyCustomDamage">
          Dañar
        </ActionButton>
        <ActionButton :disabled="busy || !isCustomAmountValid" variant="success" @click="applyCustomHeal">
          Curar
        </ActionButton>
        <ActionButton :disabled="busy" variant="danger" @click="$emit('remove')">Eliminar</ActionButton>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import type { ActiveSummonInstance } from '@/types/combat';

const props = defineProps<{
  instance: ActiveSummonInstance;
  defenseSummary?: string;
  busy?: boolean;
}>();

const customAmount = ref<number | null>(1);
const isCustomAmountValid = computed(() => Number.isInteger(customAmount.value) && (customAmount.value ?? 0) >= 1);
const visibleDefenseSummary = computed(() => props.defenseSummary?.trim() || '—');
const emit = defineEmits<{
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

function applyDelta(amount: number): void {
  if (amount < 0) {
    emit('damage', Math.abs(amount));
    return;
  }

  emit('heal', amount);
}

function applyCustomDamage(): void {
  if (!isCustomAmountValid.value || customAmount.value === null) {
    return;
  }

  emit('damage', customAmount.value);
}

function applyCustomHeal(): void {
  if (!isCustomAmountValid.value || customAmount.value === null) {
    return;
  }

  emit('heal', customAmount.value);
}

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
.defense-line,
.muted {
  margin: 0.45rem 0 0;
  color: #cbd5e1;
}

.defense-line {
  font-size: 0.84rem;
}

.muted {
  font-size: 0.82rem;
  color: #94a3b8;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 0.4rem;
  margin-top: 0.75rem;
}

.mini-button {
  min-height: 2.7rem;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 0.75rem;
  color: #f8fafc;
  font-weight: 700;
}

.mini-button.danger {
  background: rgba(185, 28, 28, 0.9);
}

.mini-button.positive {
  background: rgba(22, 101, 52, 0.95);
}

.custom-row {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.1fr);
  gap: 0.6rem;
  align-items: end;
  margin-top: 0.75rem;
}

.custom-input {
  display: grid;
  gap: 0.35rem;
}

.custom-input input {
  min-height: 3rem;
  border-radius: 0.85rem;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(15, 23, 42, 0.85);
  color: inherit;
  padding: 0 0.85rem;
  max-width: 8rem;
}

.custom-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr)) minmax(7rem, auto);
  gap: 0.5rem;
  align-items: stretch;
}

.button-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 0.5rem;
  margin-top: 0.75rem;
}

@media (max-width: 720px) {
  .quick-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .custom-row {
    grid-template-columns: 1fr;
  }

  .custom-input input {
    max-width: none;
  }

  .custom-actions {
    grid-template-columns: 1fr;
  }
}
</style>
