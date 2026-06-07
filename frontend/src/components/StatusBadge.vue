<template>
  <span :class="['status-badge', `status-badge--${variant}`]">
    <slot />
  </span>
</template>

<script setup lang="ts">
defineProps<{
  variant?: 'neutral' | 'success' | 'warning' | 'danger';
}>();
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.5rem;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 700;
}

.status-badge--neutral {
  background: rgba(148, 163, 184, 0.15);
  color: #e2e8f0;
}

.status-badge--success {
  background: rgba(16, 185, 129, 0.18);
  color: #6ee7b7;
}

.status-badge--warning {
  background: rgba(245, 158, 11, 0.2);
  color: #fbbf24;
}

.status-badge--danger {
  background: rgba(239, 68, 68, 0.18);
  color: #fca5a5;
}
</style>
