<template>
  <header class="topbar">
    <div>
      <p class="eyebrow">Pathfinder 1e · Inquisidor Monster Tactician</p>
      <h1>Kazkum Koldugum Summons</h1>
    </div>

    <div class="topbar-actions">
      <nav class="topbar-nav" aria-label="Navegación principal">
        <RouterLink to="/">Combate</RouterLink>
        <RouterLink to="/invocaciones">Invocaciones</RouterLink>
        <RouterLink to="/catalogo">Catálogo</RouterLink>
      </nav>

      <div class="topbar-right">
        <RouterLink class="settings-link" to="/configuracion">Configuración</RouterLink>

        <button
          class="clear-button"
          type="button"
          :disabled="!hasSummons || store.busy"
          @click="handleClearSummons"
        >
          Limpiar
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useCombatStore } from '@/stores/combat';

const store = useCombatStore();
const hasSummons = computed(() => store.groups.length > 0);

async function handleClearSummons(): Promise<void> {
  if (!hasSummons.value || store.busy) {
    return;
  }

  const confirmed = window.confirm('¿Seguro que quieres limpiar todas las invocaciones activas?');
  if (!confirmed) {
    return;
  }

  await store.clearSummons();
}
</script>

<style scoped>
.topbar {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(10, 14, 22, 0.8);
  backdrop-filter: blur(12px);
}

.eyebrow {
  margin: 0 0 0.25rem;
  color: #94a3b8;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  font-size: 1.25rem;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: nowrap;
  justify-content: flex-end;
  min-width: 0;
}

.topbar-nav {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-left: auto;
  flex-shrink: 0;
}

.topbar-nav a {
  text-decoration: none;
  padding: 0.55rem 0.8rem;
  border-radius: 0.75rem;
  background: rgba(30, 41, 59, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.15);
}

.settings-link {
  text-decoration: none;
  padding: 0.55rem 0.8rem;
  border-radius: 0.75rem;
  background: rgba(30, 41, 59, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.15);
}

.topbar-nav a.router-link-active {
  background: rgba(79, 70, 229, 0.35);
  border-color: rgba(129, 140, 248, 0.45);
}

.settings-link.router-link-active {
  background: rgba(79, 70, 229, 0.35);
  border-color: rgba(129, 140, 248, 0.45);
}

.clear-button {
  min-height: 2.7rem;
  padding: 0.6rem 0.9rem;
  border-radius: 0.75rem;
  border: 1px solid rgba(239, 68, 68, 0.28);
  background: rgba(127, 29, 29, 0.7);
  color: #fecaca;
  font-weight: 600;
}

.clear-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

@media (max-width: 720px) {
  .topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .topbar-actions {
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .topbar-nav {
    justify-content: flex-start;
  }

  .topbar-right {
    margin-left: 0;
  }
}
</style>
