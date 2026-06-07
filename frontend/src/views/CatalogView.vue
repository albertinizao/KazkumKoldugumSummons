<template>
  <section class="page">
    <article class="card">
      <div class="section-title">
        <div>
          <p class="eyebrow">Catálogo</p>
          <h2>Base de criaturas</h2>
        </div>
        <span class="chip accent">{{ filteredCreatures.length }} / {{ creatureCatalog.length }}</span>
      </div>

      <div class="toolbar">
        <label>
          <span class="muted small">Buscar</span>
          <input v-model="query" placeholder="Wolf, elemental, archon..." />
        </label>
        <label>
          <span class="muted small">Nivel</span>
          <select v-model="levelFilter">
            <option value="">Todos</option>
            <option v-for="level in levels" :key="level" :value="String(level)">Nivel {{ level }}</option>
          </select>
        </label>
        <label>
          <span class="muted small">Plantilla</span>
          <select v-model="templateFilter">
            <option value="">Todas</option>
            <option v-for="template in templates" :key="template.key" :value="template.key">
              {{ template.label }}
            </option>
          </select>
        </label>
      </div>
    </article>

    <div class="page-grid">
      <section class="stack">
        <CreatureCard
          v-for="creature in filteredCreatures"
          :key="creature.id"
          :creature="creature"
          :max-summon-monster-level="settings.maxSummonMonsterLevel"
          @preview="previewCreature"
          @summon="summonCreature"
        />

        <EmptyState
          v-if="filteredCreatures.length === 0"
          title="No hay coincidencias"
          description="Prueba a relajar los filtros o cambia el nivel máximo configurado."
        />
      </section>

      <aside class="stack">
        <article class="card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Vista previa</p>
              <h2>{{ preview.name }}</h2>
            </div>
            <span class="chip success">{{ preview.templateLabel }}</span>
          </div>

          <p class="muted">
            Esta vista previa aplica la plantilla seleccionada y muestra los cambios importantes antes de invocar.
          </p>

          <div class="meta">
            <div class="meta-item">
              <span>Alinhación</span>
              <strong>{{ preview.finalAlignment }}</strong>
            </div>
            <div class="meta-item">
              <span>CA</span>
              <strong>{{ preview.defenses.ac }}</strong>
            </div>
            <div class="meta-item">
              <span>PG</span>
              <strong>{{ preview.defenses.hp }}</strong>
            </div>
            <div class="meta-item">
              <span>Synergy</span>
              <strong>{{ preview.isEarthSynergy ? '+1 ataque / +1 CA' : '—' }}</strong>
            </div>
          </div>

          <div class="divider"></div>

          <div class="stack small">
            <div>
              <strong>Velocidades</strong>
              <p class="muted">{{ preview.speedLines.join(' · ') }}</p>
            </div>
            <div>
              <strong>Notas</strong>
              <ul class="muted">
                <li v-for="note in preview.notes" :key="note">{{ note }}</li>
              </ul>
            </div>
          </div>

          <div class="divider"></div>

          <div class="row-actions">
            <button class="secondary" @click="combat.openSummonModal(previewBaseCreature)">Invocar desde vista previa</button>
          </div>
        </article>

        <article class="card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Persistencia</p>
              <h3>Historial y favoritos</h3>
            </div>
          </div>

          <div class="stack">
            <div>
              <strong>Recientes</strong>
              <p class="muted small">{{ combat.recentSummons.length }} invocaciones recientes persistidas</p>
            </div>
            <div>
              <strong>Populares</strong>
              <p class="muted small">{{ combat.popularSummons.length }} entradas de uso persistidas</p>
            </div>
          </div>
        </article>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { creatureCatalog, summonTemplates } from '@/data/catalog'
import { resolveCreature } from '@/utils/summoning'
import CreatureCard from '@/components/CreatureCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { useCombatStore } from '@/stores/combat'
import { useSettingsStore } from '@/stores/settings'

const combat = useCombatStore()
const settings = useSettingsStore()

const query = ref('')
const levelFilter = ref('')
const templateFilter = ref('')
const previewBaseCreature = ref(creatureCatalog[0])
const previewTemplate = ref('none')

const levels = computed(() => [...new Set(creatureCatalog.map(creature => creature.summonLevel))].sort((a, b) => a - b))
const templates = summonTemplates.filter(template => template.key !== 'none')

const filteredCreatures = computed(() => {
  const normalized = query.value.trim().toLowerCase()
  return creatureCatalog.filter(creature => {
    const matchesQuery = !normalized || [
      creature.name,
      creature.type,
      creature.subtype ?? '',
      creature.tags.join(' '),
      creature.notes
    ].some(value => value.toLowerCase().includes(normalized))
    const matchesLevel = !levelFilter.value || creature.summonLevel === Number(levelFilter.value)
    const matchesTemplate = !templateFilter.value || creature.allowedTemplates.includes(templateFilter.value)
    return matchesQuery && matchesLevel && matchesTemplate
  })
})

const preview = computed(() => resolveCreature(previewBaseCreature.value, previewTemplate.value))

function previewCreature(creature) {
  previewBaseCreature.value = creature
  previewTemplate.value = creature.allowedTemplates[0] ?? 'none'
}

function summonCreature(creature) {
  combat.openSummonModal(creature, creature.allowedTemplates[0] ?? 'none')
}
</script>
