<template>
  <article class="group-card">
    <header class="group-header">
      <div>
        <h3>{{ name }}</h3>
        <p>{{ count }} criaturas activas</p>
      </div>
      <StatusBadge variant="success">Grupo activo</StatusBadge>
    </header>

    <div class="summary-grid">
      <div>
        <span>CA</span>
        <strong>—</strong>
      </div>
      <div>
        <span>PG</span>
        <strong>—</strong>
      </div>
      <div>
        <span>TS</span>
        <strong>—</strong>
      </div>
      <div>
        <span>Ataques</span>
        <strong>—</strong>
      </div>
    </div>

    <div class="button-row">
      <ActionButton>Atacar con todas</ActionButton>
      <ActionButton>Tirar TS</ActionButton>
      <ActionButton>Expandir ficha</ActionButton>
    </div>
  </article>
</template>

<script setup lang="ts">
import ActionButton from '@/components/ActionButton.vue';
import StatusBadge from '@/components/StatusBadge.vue';

defineProps<{
  name: string;
  count: number;
}>();
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

h3,
p {
  margin: 0;
}

p {
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
  font-size: 1.1rem;
}

.button-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
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
