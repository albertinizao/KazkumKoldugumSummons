<template>
  <section class="page">
    <div class="page-grid">
      <SettingsPanel />

      <article class="card">
        <div class="section-title">
          <div>
            <p class="eyebrow">Estado persistido</p>
            <h2>Resumen del sistema</h2>
          </div>
        </div>

        <div class="stack">
          <div class="meta-item">
            <span>Max summon monster level</span>
            <strong>{{ settings.maxSummonMonsterLevel }}</strong>
          </div>
          <div class="meta-item">
            <span>Usos diarios</span>
            <strong>{{ combat.dailyUses.remaining }} / {{ combat.dailyUses.maximum }}</strong>
          </div>
          <div class="meta-item">
            <span>Grupos activos</span>
            <strong>{{ combat.groups.length }}</strong>
          </div>
          <div class="meta-item">
            <span>Último guardado</span>
            <strong>{{ settings.lastSavedAt ? new Date(settings.lastSavedAt).toLocaleString() : '—' }}</strong>
          </div>
        </div>

        <div class="divider"></div>

        <p class="muted small">
          La configuración viaja al backend. El resto del estado del combate permanece en el navegador con persistencia local.
        </p>
      </article>
    </div>
  </section>
</template>

<script setup>
import SettingsPanel from '@/components/SettingsPanel.vue'
import { useSettingsStore } from '@/stores/settings'
import { useCombatStore } from '@/stores/combat'

const settings = useSettingsStore()
const combat = useCombatStore()
</script>
