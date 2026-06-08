<template>
  <article class="card">
    <div class="section-title">
      <div>
        <p class="eyebrow">Persistencia</p>
        <h2>Configuración de invocación</h2>
      </div>
      <span class="chip accent">Backend</span>
    </div>

    <p class="muted">
      El valor <code>maxSummonMonsterLevel</code> se guarda en el backend y se usa para filtrar criaturas y calcular la cantidad de invocación.
    </p>

    <div class="grid-2">
      <label>
        <span class="muted small">Nivel máximo de Summon Monster</span>
        <input type="number" min="0" step="1" v-model.number="draft" />
      </label>
      <label>
        <span class="muted small">Usos diarios máximos</span>
        <input type="number" min="0" step="1" v-model.number="dailyUsesDraft" />
      </label>
      <div class="stack">
        <button @click="save" :disabled="settings.saving">
          {{ settings.saving ? 'Guardando…' : 'Guardar cambios' }}
        </button>
        <button class="secondary" @click="reload" :disabled="settings.loading">
          {{ settings.loading ? 'Cargando…' : 'Recargar desde backend' }}
        </button>
      </div>
    </div>

    <div class="divider"></div>

    <div class="meta">
      <div class="meta-item">
        <span>Valor actual</span>
        <strong>{{ settings.maxSummonMonsterLevel }}</strong>
      </div>
      <div class="meta-item">
        <span>Usos diarios</span>
        <strong>{{ settings.dailyUses.remaining }} / {{ settings.dailyUses.maximum }}</strong>
      </div>
      <div class="meta-item">
        <span>Último guardado</span>
        <strong>{{ settings.lastSavedAt ? formatTime(settings.lastSavedAt) : '—' }}</strong>
      </div>
      <div class="meta-item">
        <span>Estado</span>
        <strong>{{ settings.error ? 'Error' : settings.loaded ? 'Listo' : 'Pendiente' }}</strong>
      </div>
    </div>

    <p v-if="settings.error" class="muted small" style="color: var(--danger); margin-top: 0.75rem;">
      {{ settings.error }}
    </p>
  </article>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useSettingsStore } from '@/stores/settings'

const settings = useSettingsStore()
const draft = ref(settings.maxSummonMonsterLevel)
const dailyUsesDraft = ref(settings.dailyUses.maximum)

watch(
  () => settings.maxSummonMonsterLevel,
  value => {
    draft.value = value
  },
  { immediate: true }
)

watch(
  () => settings.dailyUses.maximum,
  value => {
    dailyUsesDraft.value = value
  },
  { immediate: true }
)

onMounted(() => {
  void settings.loadConfiguration()
})

async function save() {
  await settings.updateConfiguration(draft.value, dailyUsesDraft.value)
}

async function reload() {
  await settings.loadConfiguration()
  draft.value = settings.maxSummonMonsterLevel
  dailyUsesDraft.value = settings.dailyUses.maximum
}

function formatTime(isoString) {
  return new Date(isoString).toLocaleString()
}
</script>
