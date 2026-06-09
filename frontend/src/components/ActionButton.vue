<template>
  <button class="action-button" :class="buttonClass" type="button" :disabled="disabled">
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  disabled?: boolean;
  variant?: 'primary' | 'danger' | 'neutral' | 'success';
}>(), {
  variant: 'primary',
});

const buttonClass = computed(() => {
  if (props.variant === 'primary') {
    return undefined;
  }

  return `action-button--${props.variant}`;
});
</script>

<style scoped>
.action-button {
  min-height: 3rem;
  padding: 0.8rem 1rem;
  border-radius: 0.9rem;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: linear-gradient(180deg, rgba(51, 65, 85, 0.9), rgba(30, 41, 59, 0.95));
  color: #f8fafc;
  font-weight: 700;
  cursor: pointer;
}

.action-button:hover {
  border-color: rgba(129, 140, 248, 0.55);
}

.action-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.action-button--danger {
  background: linear-gradient(135deg, #dc2626, #7f1d1d);
}

.action-button--success {
  background: linear-gradient(135deg, #15803d, #14532d);
}

.action-button--neutral {
  background: rgba(30, 41, 59, 0.9);
}
</style>
