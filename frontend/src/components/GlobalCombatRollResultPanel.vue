<template>
  <article class="global-roll-panel card">
    <header class="global-roll-panel__header">
      <div>
        <p class="eyebrow">{{ kindLabel }}</p>
        <h3>{{ result.title }}</h3>
        <p class="muted">{{ result.results.length }} grupo(s)</p>
      </div>
      <StatusBadge :variant="badgeVariant">{{ badgeLabel }}</StatusBadge>
    </header>

    <div class="global-roll-results">
      <CombatRollResultPanel
        v-for="rollResult in result.results"
        :key="rollResult.id"
        :result="rollResult"
        :show-raw="false"
      />
    </div>

    <section class="raw-section" aria-labelledby="raw-result-title">
      <div class="raw-section__header">
        <p class="eyebrow">RAW</p>
        <h4 id="raw-result-title">Resultado bruto</h4>
      </div>
      <pre class="raw-output">{{ result.displayText }}</pre>
    </section>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import CombatRollResultPanel from '@/components/CombatRollResultPanel.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import type { GlobalCombatRollResult } from '@/types/combat';

const props = defineProps<{
  result: GlobalCombatRollResult;
}>();

const isAttackResult = computed(() => props.result.results.some(result => result.type === 'ATTACK_GROUP'));
const kindLabel = computed(() => (isAttackResult.value ? 'Ataques globales' : 'TS globales'));
const badgeLabel = computed(() => (isAttackResult.value ? 'Ataque' : 'TS'));
const badgeVariant = computed(() => (isAttackResult.value ? 'warning' : 'success'));
</script>

<style scoped>
.global-roll-panel {
  display: grid;
  gap: 1rem;
}

.global-roll-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: start;
}

.global-roll-results {
  display: grid;
  gap: 1rem;
}

.raw-section {
  display: grid;
  gap: 0.75rem;
  padding-top: 0.25rem;
  border-top: 1px solid rgba(148, 163, 184, 0.14);
}

.raw-section__header h4 {
  margin: 0.2rem 0 0;
}

.raw-output {
  margin: 0;
  padding: 0.9rem;
  border-radius: 0.85rem;
  background: rgba(2, 6, 23, 0.45);
  border: 1px solid rgba(148, 163, 184, 0.14);
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 720px) {
  .global-roll-panel__header {
    flex-direction: column;
  }
}
</style>
