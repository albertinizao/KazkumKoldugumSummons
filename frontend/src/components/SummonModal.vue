<template>
  <teleport to="body">
    <div v-if="combat.summonModalOpen" class="modal-backdrop" @click.self="combat.closeSummonModal">
      <section class="modal" role="dialog" aria-modal="true" aria-labelledby="summon-title">
        <div class="modal-header">
          <div>
            <p class="eyebrow">Invocar criatura</p>
            <h2 id="summon-title">{{ selectedCreature?.name ?? 'Sin criatura seleccionada' }}</h2>
            <p class="muted">{{ selectedCreature?.notes }}</p>
          </div>
          <button class="ghost" @click="combat.closeSummonModal">Cerrar</button>
        </div>

        <div class="modal-grid">
          <div class="stack">
            <article class="card">
              <div class="section-title">
                <div>
                  <p class="eyebrow">Selección</p>
                  <h3>Base y plantilla</h3>
                </div>
                <span class="chip" :class="availableBadgeClass">{{ quantityInfo?.available ? 'Disponible' : 'Bloqueada' }}</span>
              </div>

              <label>
                <span class="muted small">Criatura</span>
                <select v-model="selectedCreatureId" @change="onCreatureChange">
                  <option v-for="creature in catalog" :key="creature.id" :value="creature.id">
                    {{ creature.name }} · nivel {{ creature.summonLevel }}
                  </option>
                </select>
              </label>

              <div class="divider"></div>

              <label>
                <span class="muted small">Plantilla</span>
                <select v-model="selectedTemplateKey" @change="onTemplateChange">
                  <option v-for="template in templateOptions" :key="template.key" :value="template.key">
                    {{ template.label }} — {{ template.short }}
                  </option>
                </select>
              </label>

              <div class="divider"></div>

              <div class="grid-2">
                <div class="meta-item">
                  <span>Nivel de invocación</span>
                  <strong>{{ selectedCreature?.summonLevel ?? '—' }}</strong>
                </div>
                <div class="meta-item">
                  <span>Fórmula</span>
                  <strong>{{ quantityInfo?.formula ?? '—' }}</strong>
                </div>
                <div class="meta-item">
                  <span>Máximo posible</span>
                  <strong>{{ quantityInfo?.maximumPossibleQuantity ?? '—' }}</strong>
                </div>
                <div class="meta-item">
                  <span>Cantidad rodada</span>
                  <strong>{{ quantityInfo?.rolledQuantity ?? '—' }}</strong>
                </div>
              </div>
            </article>

            <article class="card">
              <div class="section-title">
                <div>
                  <p class="eyebrow">Previsualización</p>
                  <h3>{{ preview?.name ?? '—' }}</h3>
                </div>
                <span class="chip success">{{ preview?.templateLabel ?? 'Sin plantilla' }}</span>
              </div>

              <div class="meta">
                <div class="meta-item">
                  <span>Alinhación</span>
                  <strong>{{ preview?.finalAlignment ?? '—' }}</strong>
                </div>
                <div class="meta-item">
                  <span>CA final</span>
                  <strong>{{ preview?.defenses?.ac ?? '—' }}</strong>
                </div>
                <div class="meta-item">
                  <span>PG</span>
                  <strong>{{ preview?.defenses?.hp ?? '—' }}</strong>
                </div>
                <div class="meta-item">
                  <span>Bonificación</span>
                  <strong>{{ preview?.isEarthSynergy ? '+1 ataque / +1 CA' : '—' }}</strong>
                </div>
              </div>

              <div class="divider"></div>

              <div class="stat-list" v-if="preview">
                <div class="stat">
                  <span>Subtype</span>
                  <strong>{{ preview.finalSubtype }}</strong>
                </div>
                <div class="stat">
                  <span>Velocidades</span>
                  <strong>{{ preview.speedLines.join(' · ') }}</strong>
                </div>
                <div class="stat" v-for="attack in preview.attacks" :key="attack.name">
                  <span>{{ attack.name }}</span>
                  <strong>{{ attack.bonus }} / {{ attack.damage }}</strong>
                </div>
              </div>

              <div class="divider"></div>

              <ul class="muted small">
                <li v-for="note in preview?.notes ?? []" :key="note">{{ note }}</li>
              </ul>
            </article>
          </div>

          <div class="stack">
            <article class="card">
              <div class="section-title">
                <div>
                  <p class="eyebrow">Recursos</p>
                  <h3>Usos diarios</h3>
                </div>
                <span class="chip warning">{{ combat.dailyUses.remaining }} / {{ combat.dailyUses.maximum }}</span>
              </div>

              <p class="muted small">La invocación resta 1 uso, pero nunca baja de cero.</p>

              <div class="meta">
                <div class="meta-item">
                  <span>Restantes</span>
                  <strong>{{ combat.dailyUses.remaining }}</strong>
                </div>
                <div class="meta-item">
                  <span>Máximo</span>
                  <strong>{{ combat.dailyUses.maximum }}</strong>
                </div>
              </div>
            </article>

            <article class="card">
              <div class="section-title">
                <div>
                  <p class="eyebrow">Impacto</p>
                  <h3>Qué ocurrirá al confirmar</h3>
                </div>
              </div>

              <ol class="muted small">
                <li>La cantidad se calcula desde la API del backend.</li>
                <li>Se rodará la fórmula y se crearán instancias independientes.</li>
                <li>Si ya existe un grupo con la misma identidad final, se añadirá a ese grupo.</li>
                <li>Los usos diarios se reducirán en 1 sin bajar de cero.</li>
                <li>La selección quedará guardada en el historial reciente y popular.</li>
              </ol>
            </article>

              <div class="row-actions">
              <button class="secondary" @click="combat.closeSummonModal">Cancelar</button>
              <button :disabled="!quantityInfo?.available" @click="confirmSummon">
                {{ quantityInfo?.available ? 'Confirmar invocación' : 'Nivel bloqueado' }}
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </teleport>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { creatureCatalog, summonTemplates } from '@/data/catalog'
import { useCombatStore } from '@/stores/combat'

const combat = useCombatStore()

const catalog = creatureCatalog
const templateOptions = computed(() => {
  const creature = selectedCreature.value
  if (!creature) return summonTemplates
  const allowed = creature.allowedTemplates.includes('none') ? creature.allowedTemplates : ['none', ...creature.allowedTemplates]
  return summonTemplates.filter(template => allowed.includes(template.key))
})

const selectedCreatureId = ref(combat.selectedCreatureId)
const selectedTemplateKey = ref(combat.selectedTemplateKey)

const selectedCreature = computed(() => creatureCatalog.find(creature => creature.id === selectedCreatureId.value) ?? null)
const preview = computed(() => combat.summonPreview)
const quantityInfo = computed(() => combat.pendingQuantity)

watch(
  () => combat.summonModalOpen,
  open => {
    if (open) {
      selectedCreatureId.value = combat.selectedCreatureId
      selectedTemplateKey.value = combat.selectedTemplateKey
      combat.preparePreview()
    }
  }
)

watch(selectedCreatureId, creatureId => {
  combat.selectedCreatureId = creatureId
  const creature = creatureCatalog.find(item => item.id === creatureId)
  selectedTemplateKey.value = creature?.allowedTemplates?.[0] ?? 'none'
  combat.selectedTemplateKey = selectedTemplateKey.value
  combat.preparePreview()
})

watch(selectedTemplateKey, templateKey => {
  combat.setSelectedTemplateKey(templateKey)
})

function onCreatureChange(event) {
  selectedCreatureId.value = event.target.value
}

function onTemplateChange(event) {
  selectedTemplateKey.value = event.target.value
}

function confirmSummon() {
  combat.summonSelectedCreature()
}

const availableBadgeClass = computed(() => (quantityInfo.value?.available ? 'success' : 'danger'))
</script>
