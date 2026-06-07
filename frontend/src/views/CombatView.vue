<template>
  <section class="page">
    <div class="page-grid">
      <DailyUsesPanel />

      <article class="card">
        <div class="section-title">
          <div>
            <p class="eyebrow">Acciones</p>
            <h2>Flujo de combate</h2>
          </div>
          <span class="chip accent">Modo tablet</span>
        </div>

        <p class="muted">
          Desde aquí se abre el flujo de invocación, se administra el estado activo y se preserva la información al recargar.
        </p>

        <div class="row-actions">
          <button @click="openSummon">Invocar</button>
          <button class="secondary" @click="combat.clearAllGroups">Limpiar invocaciones</button>
        </div>

        <div class="divider"></div>

        <div class="meta">
          <div class="meta-item">
            <span>Grupos activos</span>
            <strong>{{ combat.groups.length }}</strong>
          </div>
          <div class="meta-item">
            <span>Instancias totales</span>
            <strong>{{ combat.activeInstanceCount }}</strong>
          </div>
          <div class="meta-item">
            <span>Último mensaje</span>
            <strong>{{ combat.statusMessage || '—' }}</strong>
          </div>
          <div class="meta-item">
            <span>Configuración</span>
            <strong>Max level {{ settings.maxSummonMonsterLevel }}</strong>
          </div>
        </div>
      </article>
    </div>

    <section class="card">
      <div class="section-title">
        <div>
          <p class="eyebrow">Grupos activos</p>
          <h2>Estado del combate</h2>
        </div>
      </div>

      <EmptyState
        v-if="combat.groups.length === 0"
        title="No hay invocaciones activas"
        description="Usa el botón Invocar para crear el primer grupo de criaturas."
      />

      <div v-else class="stack">
        <ActiveGroupCard
          v-for="group in combat.groups"
          :key="group.identity"
          :group="group"
          @damage="combat.damageInstance"
          @heal="combat.healInstance"
          @remove="combat.removeInstance"
        />
      </div>
    </section>

    <section class="page-grid">
      <article class="card">
        <div class="section-title">
          <div>
            <p class="eyebrow">Historial</p>
            <h2>Invocaciones recientes</h2>
          </div>
        </div>

        <EmptyState
          v-if="combat.recentSummons.length === 0"
          title="Sin historial reciente"
          description="Las últimas invocaciones aparecerán aquí para reusarlas rápidamente."
        />

        <div v-else class="stack">
          <div v-for="item in combat.recentSummons" :key="item.identity" class="table-row">
            <div>
              <strong>{{ item.creatureName }}</strong>
              <div class="muted small">{{ item.templateLabel }} · {{ item.createdAt }}</div>
            </div>
            <div class="chips">
              <span class="chip">{{ item.templateKey }}</span>
            </div>
            <div class="muted small">Grupo {{ item.identity }}</div>
            <div class="row-actions">
              <button class="secondary" @click="openSummonShortcut(item)">Reusar</button>
            </div>
          </div>
        </div>
      </article>

      <article class="card">
        <div class="section-title">
          <div>
            <p class="eyebrow">Popular</p>
            <h2>Invocaciones más usadas</h2>
          </div>
        </div>

        <EmptyState
          v-if="combat.popularSummons.length === 0"
          title="Sin ranking todavía"
          description="Aquí aparecerán las criaturas más invocadas."
        />

        <div v-else class="stack">
          <div v-for="item in combat.popularSummons" :key="item.identity" class="table-row compact">
            <div>
              <strong>{{ item.creatureName }}</strong>
              <div class="muted small">{{ item.templateLabel }}</div>
            </div>
            <div class="chips">
              <span class="chip success">{{ item.count }} usos</span>
            </div>
            <div class="row-actions">
              <button class="secondary" @click="openSummonShortcut(item)">Invocar de nuevo</button>
            </div>
          </div>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup>
import { useCombatStore } from '@/stores/combat'
import { useSettingsStore } from '@/stores/settings'
import DailyUsesPanel from '@/components/DailyUsesPanel.vue'
import EmptyState from '@/components/EmptyState.vue'
import ActiveGroupCard from '@/components/ActiveGroupCard.vue'
import { creatureCatalog } from '@/data/catalog'

const combat = useCombatStore()
const settings = useSettingsStore()

function openSummon() {
  combat.openSummonModal(creatureCatalog[0])
}

function openSummonShortcut(entry) {
  const creature = creatureCatalog.find(item => item.id === entry.creatureId)
  combat.openSummonModal(creature, entry.templateKey)
}
</script>
