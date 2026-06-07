<template>
  <section class="combat-view">
    <DailyUsesPanel />

    <section class="content-grid">
      <div class="column">
        <h2>Resultado más reciente</h2>
        <EmptyState
          title="Sin tiradas recientes"
          description="Cuando el backend devuelva el último resultado, aparecerá aquí."
        />
      </div>

      <div class="column">
        <h2>Grupos activos</h2>
        <EmptyState
          v-if="groups.length === 0"
          title="No hay invocaciones activas"
          description="Este es el estado inicial del esqueleto. Aquí se renderizarán los grupos de criaturas activas."
        />
        <div v-else class="groups-list">
          <CombatGroupCard
            v-for="group in groups"
            :key="group.id"
            :name="group.name"
            :count="group.count"
          />
        </div>
      </div>
    </section>

    <section class="cards-preview">
      <h2>Cards individuales</h2>
      <div class="cards-grid">
        <CreatureCard label="Criatura 1" :current-hit-points="15" :max-hit-points="15" state="HEALTHY" />
        <CreatureCard label="Criatura 2" :current-hit-points="7" :max-hit-points="15" state="DAMAGED" />
        <CreatureCard label="Criatura 3" :current-hit-points="0" :max-hit-points="15" state="DOWN" />
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useCombatStore } from '@/stores/combat';
import DailyUsesPanel from '@/components/DailyUsesPanel.vue';
import CombatGroupCard from '@/components/CombatGroupCard.vue';
import CreatureCard from '@/components/CreatureCard.vue';
import EmptyState from '@/components/EmptyState.vue';

const store = useCombatStore();
const groups = computed(() => store.groups);
</script>

<style scoped>
.combat-view {
  display: grid;
  gap: 1rem;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1.35fr;
  gap: 1rem;
}

.column,
.cards-preview {
  display: grid;
  gap: 0.75rem;
}

h2 {
  margin: 0;
  font-size: 1.05rem;
}

.groups-list,
.cards-grid {
  display: grid;
  gap: 0.75rem;
}

.cards-grid {
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

@media (max-width: 960px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
