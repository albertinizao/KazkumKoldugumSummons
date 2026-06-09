<template>
  <section class="catalog-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Catálogo</p>
        <h2>Criaturas base invocables</h2>
        <p class="muted">
          Busca por nombre, filtra por nivel de invocación y previsualiza la criatura final antes de
          usarla en mesa.
        </p>
      </div>

      <div class="stats-row">
        <div class="stat-card">
          <span>Total</span>
          <strong>{{ total }}</strong>
        </div>
        <div class="stat-card">
          <span>Filtradas</span>
          <strong>{{ visibleCount }}</strong>
        </div>
      </div>
    </header>

    <section class="filters card">
      <label>
        <span>Buscar</span>
        <input v-model.trim="query" type="search" placeholder="Badger, elemental, wolf..." />
      </label>

      <label>
        <span>Nivel exacto</span>
        <select v-model="summonLevel">
          <option :value="null">Todos</option>
          <option v-for="level in summonLevels" :key="level" :value="level">{{ level }}</option>
        </select>
      </label>

      <label>
        <span>Nivel máximo</span>
        <select v-model="maxSummonLevel">
          <option :value="null">Sin filtro</option>
          <option v-for="level in summonLevels" :key="level" :value="level">{{ level }}</option>
        </select>
      </label>

      <label>
        <span>Plantilla</span>
        <select v-model="templateKey">
          <option :value="null">Todas</option>
          <option v-for="template in summonTemplates" :key="template.key" :value="template.key">
            {{ template.label }}
          </option>
        </select>
      </label>
    </section>

    <section class="layout">
      <article class="card creature-list">
        <div class="section-header">
          <div>
            <p class="eyebrow">Resultados</p>
            <h3>Listado</h3>
          </div>
          <button class="ghost" type="button" @click="reload">Recargar</button>
        </div>

        <p v-if="loading" class="muted">Cargando catálogo...</p>
        <p v-else-if="error" class="error">{{ error }}</p>
        <div v-else-if="items.length === 0" class="empty">
          No hay criaturas que coincidan con el filtro actual.
        </div>

        <div v-else class="list">
          <button
            v-for="item in items"
            :key="item.id"
            class="creature-row"
            :class="{ active: item.id === selectedCreatureId }"
            type="button"
            @click="selectCreature(item.id)"
          >
            <div class="creature-row__main">
              <strong>{{ item.name }}</strong>
              <span class="muted">{{ item.creatureType }} · nivel {{ item.summonLevel }}</span>
            </div>
            <div class="creature-row__meta">
              <span class="chip">{{ item.alignment }}</span>
              <span class="chip">{{ item.size }}</span>
              <span class="chip">{{ item.summary.maxHitPoints }} PG</span>
            </div>
          </button>
        </div>
      </article>

      <article class="card preview-panel">
        <div class="section-header">
          <div>
            <p class="eyebrow">Previsualización</p>
            <h3>{{ previewTitle }}</h3>
          </div>
          <span class="chip success">{{ preview?.appliedTemplate ?? 'Sin plantilla' }}</span>
        </div>

        <template v-if="selectedCreature">
            <div class="summary-grid">
            <div>
              <span>Alineación</span>
              <strong>{{ preview?.alignment ?? selectedCreature.alignment }}</strong>
            </div>
            <div>
              <span>Tamaño</span>
              <strong>{{ preview?.size ?? selectedCreature.size }}</strong>
            </div>
            <div>
              <span>CA</span>
              <strong>{{ preview?.armorClass.normal ?? selectedCreature.summary.armorClass.normal }}</strong>
            </div>
            <div>
              <span>PG</span>
              <strong>{{ preview?.maxHitPoints ?? selectedCreature.summary.maxHitPoints }}</strong>
            </div>
          </div>

          <div class="detail-grid">
            <section>
              <h4>Resumen base</h4>
              <p><strong>CA:</strong> {{ selectedCreature.summary.armorClass.normal }} / {{ selectedCreature.summary.armorClass.touch }} / {{ selectedCreature.summary.armorClass.flatFooted }}</p>
              <p><strong>TS:</strong> Fort {{ selectedCreature.summary.savingThrows.fortitude }}, Ref {{ selectedCreature.summary.savingThrows.reflex }}, Vol {{ selectedCreature.summary.savingThrows.will }}</p>
              <p><strong>Velocidades:</strong> {{ selectedCreature.summary.speedsText }}</p>
              <p><strong>Ataques:</strong> {{ selectedCreature.summary.attacksText }}</p>
            </section>

            <section>
              <h4>Ficha base</h4>
              <p class="muted small">{{ selectedCreatureDetail?.fullStatBlock ?? 'Cargando detalle...' }}</p>
            </section>
          </div>

          <div class="preview-box" v-if="preview">
            <h4>Ficha final</h4>
            <p><strong>Nombre:</strong> {{ preview.displayName }}</p>
            <p><strong>Id final:</strong> {{ preview.id }}</p>
            <p><strong>Velocidades:</strong> {{ preview.speedsText }}</p>
            <p><strong>Ataques:</strong> {{ preview.attacksText }}</p>
            <p><strong>Componentes de daño:</strong> {{ attackDamageComponentsText }}</p>
            <p><strong>Ataques especiales:</strong> {{ specialAttacksText }}</p>
            <p><strong>Defensas especiales:</strong> {{ specialDefensesText }}</p>
            <p><strong>Reglas aplicadas:</strong> {{ appliedRulesText }}</p>
            <pre class="statblock">{{ preview.fullStatBlock }}</pre>
          </div>

          <div class="template-strip">
            <span class="muted small">Plantillas permitidas:</span>
            <span v-for="template in allowedTemplates" :key="template.key" class="chip">{{ template.label }}</span>
          </div>
        </template>

        <div v-else class="empty">
          Selecciona una criatura para ver su ficha base y su previsualización final.
        </div>
      </article>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { fetchCatalogCreatures, fetchCreatureTemplate, fetchResolvedCreaturePreview } from '@/services/catalogApi';
import { summonTemplates } from '@/data/summonTemplates';
import type {
  CreatureCatalogItem,
  CreatureTemplate,
  ResolvedCreature,
  SummonTemplateType,
} from '@/types/catalog';
import { formatSpecialDefense } from '@/utils/specialDefenseDisplay';

const summonLevels = [1, 2, 3, 4, 5, 6, 7, 8, 9];

const query = ref('');
const summonLevel = ref<number | null>(null);
const maxSummonLevel = ref<number | null>(null);
const templateKey = ref<string | null>(null);
const items = ref<CreatureCatalogItem[]>([]);
const total = ref(0);
const loading = ref(false);
const error = ref('');
const selectedCreatureId = ref<string | null>(null);
const selectedCreatureDetail = ref<CreatureTemplate | null>(null);
const preview = ref<ResolvedCreature | null>(null);

const selectedCreature = computed(() => items.value.find(item => item.id === selectedCreatureId.value) ?? null);
const visibleCount = computed(() => items.value.length);
const previewTitle = computed(() => preview.value?.displayName ?? selectedCreature.value?.name ?? 'Sin selección');
const appliedRulesText = computed(() => preview.value?.appliedRules.map(rule => rule.description).join(' · ') ?? '—');
const specialAttacksText = computed(() =>
  preview.value?.specialAttacks.length
    ? preview.value.specialAttacks.join(' · ')
    : '—',
);
const attackDamageComponentsText = computed(() =>
  preview.value?.attacks.length
    ? preview.value.attacks
        .map(attack => {
          const components = attack.damageComponents
            .map(component => {
              const labelMap: Record<string, string> = {
                PIERCING: 'perforante',
                SLASHING: 'cortante',
                BLUDGEONING: 'contundente',
                FIRE: 'fuego',
                COLD: 'frío',
                ACID: 'ácido',
                ELECTRICITY: 'electricidad',
                SONIC: 'sonido',
                FORCE: 'fuerza',
                UNTYPED: 'sin tipo',
                OTHER: 'otro',
              };
              const label = labelMap[component.damageType] ?? component.damageType.toLowerCase();
              const critical = component.multipliesOnCritical ? ' (multiplica en crítico)' : '';
              return `${component.formula} ${label}${critical}`;
            })
            .join(' + ');
          return `${attack.name}: ${components}`;
        })
        .join(' · ')
    : '—',
);
const specialDefensesText = computed(() =>
  preview.value?.specialDefenses.length
    ? preview.value.specialDefenses.map(defense => formatSpecialDefense(defense)).join(' · ')
    : '—',
);
const allowedTemplates = computed(() => {
  const creatureType = selectedCreature.value?.creatureType?.toLowerCase() ?? '';
  if (creatureType.includes('outsider')) {
    return summonTemplates.filter(template => template.key === 'none');
  }

  return summonTemplates;
});

function toApiTemplate(value: string | null): SummonTemplateType | null {
  if (!value || value === 'none') {
    return null;
  }

  return value.toUpperCase() as SummonTemplateType;
}

async function reload() {
  loading.value = true;
  error.value = '';

  try {
    const response = await fetchCatalogCreatures({
      query: query.value || undefined,
      summonLevel: summonLevel.value,
      maxSummonLevel: maxSummonLevel.value,
      template: toApiTemplate(templateKey.value),
      limit: 50,
      offset: 0,
    });

    items.value = response.items;
    total.value = response.total;

    if (!items.value.some(item => item.id === selectedCreatureId.value)) {
      selectedCreatureId.value = items.value[0]?.id ?? null;
    }

    await loadSelectedCreature();
  } catch (exception) {
    error.value = exception instanceof Error ? exception.message : 'No se pudo cargar el catálogo.';
    items.value = [];
    total.value = 0;
    selectedCreatureId.value = null;
    selectedCreatureDetail.value = null;
    preview.value = null;
  } finally {
    loading.value = false;
  }
}

async function loadSelectedCreature() {
  if (!selectedCreatureId.value) {
    selectedCreatureDetail.value = null;
    preview.value = null;
    return;
  }

  try {
    const [detail, resolved] = await Promise.all([
      fetchCreatureTemplate(selectedCreatureId.value),
      fetchResolvedCreaturePreview(selectedCreatureId.value, toApiTemplate(templateKey.value)),
    ]);

    selectedCreatureDetail.value = detail;
    preview.value = resolved;
  } catch (exception) {
    error.value = exception instanceof Error ? exception.message : 'No se pudo cargar la criatura seleccionada.';
    selectedCreatureDetail.value = null;
    preview.value = null;
  }
}

function selectCreature(creatureId: string) {
  selectedCreatureId.value = creatureId;
  void loadSelectedCreature();
}

watch([query, summonLevel, maxSummonLevel, templateKey], () => {
  void reload();
});

onMounted(() => {
  void reload();
});
</script>

<style scoped>
.catalog-page {
  display: grid;
  gap: 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: end;
}

.eyebrow {
  margin: 0 0 0.25rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #94a3b8;
  font-size: 0.75rem;
}

h2,
h3,
h4,
p {
  margin: 0;
}

.muted {
  color: #94a3b8;
}

.stats-row {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.stat-card,
.card {
  border-radius: 1rem;
  background: rgba(15, 23, 42, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.stat-card {
  min-width: 110px;
  padding: 0.9rem 1rem;
}

.stat-card span,
.summary-grid span {
  display: block;
  color: #94a3b8;
  font-size: 0.78rem;
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
  padding: 1rem;
}

.filters label {
  display: grid;
  gap: 0.35rem;
}

.filters span {
  font-size: 0.82rem;
  color: #cbd5e1;
}

input,
select {
  width: 100%;
  border-radius: 0.85rem;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(2, 6, 23, 0.5);
  color: inherit;
  padding: 0.8rem 0.9rem;
}

.layout {
  display: grid;
  grid-template-columns: 0.9fr 1.2fr;
  gap: 1rem;
}

.card {
  padding: 1rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: start;
  margin-bottom: 1rem;
}

.list {
  display: grid;
  gap: 0.65rem;
}

.creature-row {
  text-align: left;
  display: grid;
  gap: 0.65rem;
  padding: 0.9rem;
  border-radius: 0.9rem;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: rgba(30, 41, 59, 0.55);
  color: inherit;
}

.creature-row.active {
  border-color: rgba(129, 140, 248, 0.7);
  background: rgba(79, 70, 229, 0.18);
}

.creature-row__main,
.creature-row__meta {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 0.28rem 0.55rem;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.14);
  color: #e2e8f0;
  font-size: 0.78rem;
}

.chip.success {
  background: rgba(16, 185, 129, 0.18);
  color: #6ee7b7;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.summary-grid > div {
  padding: 0.8rem;
  border-radius: 0.9rem;
  background: rgba(30, 41, 59, 0.55);
}

.summary-grid strong {
  font-size: 1.1rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.detail-grid section,
.preview-box {
  padding: 0.9rem;
  border-radius: 0.9rem;
  background: rgba(2, 6, 23, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.detail-grid p,
.preview-box p {
  margin-top: 0.45rem;
}

.statblock {
  margin: 0.75rem 0 0;
  padding: 0.85rem;
  border-radius: 0.75rem;
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(148, 163, 184, 0.12);
  white-space: pre-wrap;
  word-break: break-word;
}

.template-strip {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  align-items: center;
  margin-top: 1rem;
}

.empty,
.error {
  padding: 1rem;
  border-radius: 0.9rem;
  background: rgba(15, 23, 42, 0.5);
}

.error {
  color: #fca5a5;
}

@media (max-width: 980px) {
  .filters,
  .layout,
  .detail-grid,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
