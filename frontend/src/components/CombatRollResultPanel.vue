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
            <p class="roll-line roll-line--summary" :title="formatDiceTooltip(attack.attackRoll)">
              <strong>{{ formatAttackLabel(attack.attackName, attack.attackIndex) }}:</strong>&nbsp;
              <span class="roll-value">{{ attack.attackRoll.total }}</span>
              <span v-if="attack.criticalThreat" class="roll-critical-label"> (amenaza de crítico)</span>
            </p>

            <p class="roll-line roll-line--summary" :title="formatDamageTooltip(attack.normalDamage)">
              <strong>Daño:</strong>&nbsp;
              <span class="roll-value">{{ formatDamageSummary(attack.normalDamage) }}</span>
            </p>

            <div v-if="attack.criticalThreat" class="roll-critical">
              <p class="roll-line roll-line--summary" :title="formatDiceTooltip(attack.criticalThreat.confirmationRoll)">
                <strong>Confirmación crítico:</strong>&nbsp;
                <span class="roll-value">{{ attack.criticalThreat.confirmationRoll.total }}</span>
              </p>
              <p class="roll-line roll-line--summary" :title="formatDamageTooltip(attack.criticalThreat.criticalDamage)">
                <strong>Daño crítico:</strong>&nbsp;
                <span class="roll-value">{{ formatDamageSummary(attack.criticalThreat.criticalDamage) }}</span>
              </p>
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
            <p class="roll-line roll-line--summary" :title="formatDiceTooltip(instance.fortitude)">
              <strong>Fortaleza:</strong>&nbsp;
              <span class="roll-value">{{ instance.fortitude.total }}</span>
            </p>
            <p class="roll-line roll-line--summary" :title="formatDiceTooltip(instance.reflex)">
              <strong>Reflejos:</strong>&nbsp;
              <span class="roll-value">{{ instance.reflex.total }}</span>
            </p>
            <p class="roll-line roll-line--summary" :title="formatDiceTooltip(instance.will)">
              <strong>Voluntad:</strong>&nbsp;
              <span class="roll-value">{{ instance.will.total }}</span>
            </p>
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
const showRaw = computed(() => props.showRaw === true);

function formatAttackLabel(name: string, index: number | null): string {
  return index === null ? name : `${name} ${index}`;
}

function formatDiceTooltip(roll: DiceRoll): string {
  const formula = formatFormulaWithNaturalResults(roll);

  if (!formula) {
    return String(roll.total);
  }

  return formula;
}

function formatDamageSummary(result: DamageRollResult): string {
  return result.components
    .map(component => `${component.total} ${damageLabel(component.damageType)}`)
    .join(' +');
}

function formatDamageTooltip(result: DamageRollResult): string {
  return result.components
    .map(component => {
      const rolledFormula = formatFormulaWithNaturalResults(component.roll);
      const appliedFormula = component.appliedFormula ?? component.formula;
      const damageType = damageLabel(component.damageType);

      const formulaLabel = appliedFormula === component.formula ? rolledFormula : appliedFormula;
      return `${formulaLabel} -> ${component.total} ${damageType}`;
    })
    .join('\n');
}

function damageLabel(damageType: DamageComponentRollResult['damageType']): string {
  return damageType.toLowerCase();
}

function formatFormulaWithNaturalResults(roll: DiceRoll): string {
  const match = roll.formula.match(/^(\d*)[dD](\d+)([+-]\d+)?$/);
  if (!match) {
    return roll.formula;
  }

  const diceCount = match[1] === '' ? '1' : match[1];
  const dieSize = match[2];
  const modifier = match[3];
  const naturalResults = roll.naturalResults.length === 0
    ? ''
    : `[${roll.naturalResults.join(' + ')}]`;
  const modifierText = modifier
    ? ` ${modifier.startsWith('+') ? '+' : '-'} ${Math.abs(Number(modifier.slice(1)))}`
    : '';

  return `${diceCount}d${dieSize}${naturalResults}${modifierText}`;
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

.roll-line {
  margin: 0;
  color: #e2e8f0;
}

.roll-line--summary {
  cursor: help;
}

.roll-value {
  text-decoration: underline dotted rgba(148, 163, 184, 0.7);
  text-underline-offset: 0.16em;
}

.roll-critical-label {
  color: #fbbf24;
  font-weight: 600;
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
