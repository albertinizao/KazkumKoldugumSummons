<template>
  <article class="card">
    <div class="section-title">
      <div>
        <p class="eyebrow">Recursos</p>
        <h2>Usos diarios</h2>
      </div>
      <span class="chip warning">{{ combat.dailyUses.remaining }} / {{ combat.dailyUses.maximum }}</span>
    </div>

    <div class="meta">
      <div class="meta-item">
        <span>Restantes</span>
        <strong>{{ combat.dailyUses.remaining }}</strong>
      </div>
      <div class="meta-item">
        <span>Máximo</span>
        <strong>{{ combat.dailyUses.maximum }}</strong>
      </div>
      <div class="meta-item">
        <span>Invocaciones activas</span>
        <strong>{{ combat.activeInstanceCount }}</strong>
      </div>
    </div>

    <div class="divider"></div>

    <div class="toolbar">
      <label>
        <span class="muted small">Máximo</span>
        <input type="number" min="0" step="1" :value="combat.dailyUses.maximum" @change="onMaximumChange">
      </label>
      <label>
        <span class="muted small">Restantes</span>
        <input type="number" min="0" step="1" :value="combat.dailyUses.remaining" @change="onRemainingChange">
      </label>
      <div class="stack">
        <button class="secondary" @click="combat.incrementDailyUses">+1 restante</button>
        <button class="secondary" @click="combat.decrementDailyUses">-1 restante</button>
      </div>
      <button class="ghost" @click="combat.resetDailyUses">Restablecer</button>
    </div>
  </article>
</template>

<script setup>
import { useCombatStore } from '@/stores/combat'

const combat = useCombatStore()

function onMaximumChange(event) {
  combat.setDailyUsesMaximum(Number(event.target.value))
}

function onRemainingChange(event) {
  combat.setDailyUsesRemaining(Number(event.target.value))
}
</script>
