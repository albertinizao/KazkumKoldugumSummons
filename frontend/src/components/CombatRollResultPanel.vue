<template>
  <article class="roll-panel card">
    <header class="roll-panel__header">
      <div>
        <p class="eyebrow">{{ kindLabel }}</p>
        <h3>{{ result.title }}</h3>
        <p class="muted">{{ createdAtLabel }}</p>
      </div>
      <StatusBadge :variant="badgeVariant">{{ badgeLabel }}</StatusBadge>
    </header>

    <div v-if="attackResult" class="roll-result-stack">
      <section v-for="instance in attackResult.instanceResults" :key="instance.instanceId" class="roll-instance">
        <h4>{{ instance.instanceDisplayName }}</h4>

        <div class="roll-entry-stack">
          <article v-for="attack in instance.attackResults" :key="attack.attackId + ':' + String(attack.attackIndex ?? '')" class="roll-entry">
            <div class="roll-entry__header">
              <strong>{{ formatAttackName(attack.attackName, attack.attackIndex) }}</strong>
              <span class="muted">{{ formatDiceRoll(attack.attackRoll, true) }}</span>
            </div>

            <p class="roll-line"><strong>Daño si impacta:</strong> {{ formatDamageRoll(attack.normalDamage) }}</p>

            <div v-if="attack.criticalThreat" class="roll-critical">
              <p class="roll-line"><strong>Amenaza de crítico:</strong> {{ formatDiceRoll(attack.attackRoll, true) }}</p>
              <p class="roll-line"><strong>Confirmación:</strong> {{ formatDiceRoll(attack.criticalThreat.confirmationRoll, true) }}</p>
              <p class="roll-line"><strong>Daño crítico:</strong> {{ formatDamageRoll(attack.criticalThreat.criticalDamage) }}</p>
            </div>
          </article>
        </div>
      </section>
    </div>

    <div v-else-if="savingThrowResult" class="roll-result-stack">
      <section v-for="instance in savingThrowResult.instanceResults" :key="instance.instanceId" class="roll-instance">
        <h4>{{ instance.instanceDisplayName }}</h4>

        <div class="roll-entry-stack">
          <article class="roll-entry">
            <p class="roll-line"><strong>Fortaleza:</strong> {{ formatDiceRoll(instance.fortitude, true) }}</p>
            <p class="roll-line"><strong>Reflejos:</strong> {{ formatDiceRoll(instance.reflex, true) }}</p>
            <p class="roll-line"><strong>Voluntad:</strong> {{ formatDiceRoll(instance.will, true) }}</p>
          </article>
        </div>
      </section>
    </div>

    <pre v-if="showRaw" class="roll-summary">{{ result.displayText }}</pre>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import StatusBadge from '@/components/StatusBadge.vue';
import type {
  CombatRollResult,
  DamageComponentRollResult,
  DamageRollResult,
  DiceRoll,
  GroupAttackRollResult,
  GroupSavingThrowsRollResult,
} from '@/types/combat';

const props = defineProps<{
  result: CombatRollResult;
  showRaw?: boolean;
}>();

const isAttackResult = computed((): boolean => props.result.type === 'ATTACK_GROUP');
const attackResult = computed((): GroupAttackRollResult | null => (isAttackResult.value ? props.result as GroupAttackRollResult : null));
const savingThrowResult = computed((): GroupSavingThrowsRollResult | null => (!isAttackResult.value ? props.result as GroupSavingThrowsRollResult : null));
const createdAtLabel = computed(() => new Date(props.result.createdAt).toLocaleString('es-ES'));
const kindLabel = computed(() => (isAttackResult.value ? 'Ataques de grupo' : 'Tiradas de salvación'));
const badgeLabel = computed(() => (isAttackResult.value ? 'Ataque' : 'TS'));
const badgeVariant = computed(() => (isAttackResult.value ? 'warning' : 'success'));
const showRaw = computed(() => props.showRaw !== false);

function formatAttackName(name: string, index: number | null): string {
  return index === null ? name : `${name} ${index}`;
}

function formatDiceRoll(roll: DiceRoll, attackStyle = false): string {
  const formula = attackStyle && roll.formula.startsWith('1d20')
    ? 'd20'
    : roll.formula;

  const naturalResults = roll.naturalResults.length === 0
    ? '0'
    : roll.naturalResults.join(' + ');

  return `${formula} ${naturalResults} + ${roll.modifier} = ${roll.total}`;
}

function formatDamageComponent(component: DamageComponentRollResult): string {
  return `${component.formula} = ${component.total} ${component.damageType.toLowerCase()}`;
}

function formatDamageRoll(result: DamageRollResult): string {
  return result.components.map(formatDamageComponent).join(' + ');
}
</script>

<style scoped>
.roll-panel {
  display: grid;
  gap: 1rem;
}

.roll-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: start;
}

.roll-result-stack {
  display: grid;
  gap: 1rem;
}

.roll-instance {
  padding: 0.9rem;
  border-radius: 0.9rem;
  border: 1px solid rgba(51, 65, 85, 0.8);
  background: rgba(15, 23, 42, 0.55);
  display: grid;
  gap: 0.75rem;
}

.roll-instance h4 {
  margin: 0;
}

.roll-entry-stack {
  display: grid;
  gap: 0.75rem;
}

.roll-entry {
  padding: 0.85rem;
  border-radius: 0.85rem;
  background: rgba(30, 41, 59, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.12);
  display: grid;
  gap: 0.4rem;
}

.roll-entry__header {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: baseline;
}

.roll-line {
  margin: 0;
  color: #e2e8f0;
}

.roll-critical {
  display: grid;
  gap: 0.35rem;
  padding-top: 0.35rem;
  border-top: 1px solid rgba(148, 163, 184, 0.14);
}

.roll-summary {
  margin: 0;
  padding: 0.85rem;
  border-radius: 0.85rem;
  background: rgba(2, 6, 23, 0.45);
  border: 1px solid rgba(148, 163, 184, 0.14);
  white-space: pre-wrap;
}

@media (max-width: 720px) {
  .roll-panel__header,
  .roll-entry__header {
    flex-direction: column;
  }
}
</style>
