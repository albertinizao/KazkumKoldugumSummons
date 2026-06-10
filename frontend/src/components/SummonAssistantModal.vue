<template>
  <teleport to="body">
    <div v-if="open" class="modal-backdrop" @click.self="handleCancel">
      <section class="modal assistant-modal" role="dialog" aria-modal="true" :aria-labelledby="titleId">
        <div class="modal-header">
          <div>
            <p class="eyebrow">Asistente de invocación</p>
            <h2 :id="titleId">{{ currentTitle }}</h2>
            <p class="muted">Nivel máximo disponible: {{ maxSummonMonsterLevel }}</p>
          </div>

          <ActionButton variant="neutral" @click="handleCancel">Cancelar</ActionButton>
        </div>

        <div class="path-chip-list" aria-label="Ruta seleccionada">
          <span v-if="path.length === 0" class="path-chip path-chip--empty">Sin respuestas todavía</span>
          <span v-for="step in path" :key="step" class="path-chip">{{ step }}</span>
        </div>

        <div v-if="!isResolved" class="question-panel">
          <h3>{{ currentNode.title }}</h3>
          <p class="question-text">{{ currentNode.question }}</p>

          <div class="option-grid">
            <ActionButton
              v-for="option in currentNode.options"
              :key="option.label"
              variant="neutral"
              class="option-button"
              @click="selectOption(option)"
            >
              {{ option.label }}
            </ActionButton>
          </div>

          <div class="question-actions">
            <ActionButton variant="neutral" :disabled="!canGoBack" @click="handleBack">
              Volver
            </ActionButton>
          </div>
        </div>

        <div v-else class="result-panel">
          <p class="eyebrow">Sugerencia final</p>
          <h3>{{ displayName }}</h3>
          <p class="result-line">
            <strong>Nivel:</strong> {{ resolvedSuggestion?.summonLevel }}
          </p>
          <p class="result-line">
            <strong>Cantidad:</strong> {{ resolvedSuggestion?.quantityFormula }}
          </p>
          <p class="result-line">
            <strong>Criatura:</strong> {{ displayName }}
          </p>
          <p class="result-line">
            <strong>Plantilla:</strong> {{ templateLabel(resolvedSuggestion?.template) }}
          </p>
          <p class="result-line">
            <strong>Motivo:</strong> {{ reasonEs }}
          </p>

          <div class="result-actions">
            <ActionButton variant="neutral" @click="handleBack">
              Volver
            </ActionButton>
            <ActionButton variant="success" @click="handleInvoke">Invocar</ActionButton>
          </div>
        </div>
      </section>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import ActionButton from '@/components/ActionButton.vue';
import { summonAssistantNodes, type SummonAssistantNode, type SummonAssistantOption } from '@/data/summonAssistant';
import { summonAssistantChoicesNv3, type SummonAssistantChoiceNv3 } from '@/data/summonAssistantChoicesNv3';
import { summonAssistantChoicesNv4, type SummonAssistantChoiceNv4 } from '@/data/summonAssistantChoicesNv4';
import { summonAssistantChoicesNv5, type SummonAssistantChoiceNv5 } from '@/data/summonAssistantChoicesNv5';
import { summonAssistantChoicesNv6, type SummonAssistantChoiceNv6 } from '@/data/summonAssistantChoicesNv6';
import { summonAssistantChoicesNv7, type SummonAssistantChoiceNv7 } from '@/data/summonAssistantChoicesNv7';
import { summonAssistantChoicesNv8, type SummonAssistantChoiceNv8 } from '@/data/summonAssistantChoicesNv8';
import { summonAssistantChoicesNv9, type SummonAssistantChoiceNv9 } from '@/data/summonAssistantChoicesNv9';
import { getSummonAssistantDisplayName } from '@/data/summonAssistantDisplay';
import { useCombatStore } from '@/stores/combat';
import type { SummonTemplateType } from '@/types/catalog';
import { resolveSummonAssistantTemplate } from '@/utils/summonAssistantTemplateResolution';

const props = defineProps<{
  open: boolean;
  maxSummonMonsterLevel: number;
}>();

const emit = defineEmits<{
  cancel: [];
  invoke: [suggestion: SummonAssistantChoiceNv3 | SummonAssistantChoiceNv4 | SummonAssistantChoiceNv5 | SummonAssistantChoiceNv6 | SummonAssistantChoiceNv7 | SummonAssistantChoiceNv8 | SummonAssistantChoiceNv9];
}>();

const titleId = 'summon-assistant-title';
const combatStore = useCombatStore();
const nodeHistory = ref<string[]>(['root']);
const path = ref<string[]>([]);
const resolvedChoiceKey = ref<string | null>(null);

const currentNodeId = computed(() => nodeHistory.value[nodeHistory.value.length - 1] ?? 'root');
const currentNode = computed<SummonAssistantNode>(() => summonAssistantNodes[currentNodeId.value] ?? summonAssistantNodes.root);
const isResolved = computed(() => resolvedChoiceKey.value !== null);
const canGoBack = computed(() => nodeHistory.value.length > 1 || path.value.length > 0);
const activeChoiceTable = computed<Record<string, SummonAssistantChoiceNv3 | SummonAssistantChoiceNv4 | SummonAssistantChoiceNv5 | SummonAssistantChoiceNv6 | SummonAssistantChoiceNv7 | SummonAssistantChoiceNv8 | SummonAssistantChoiceNv9>>(() => {
  if (props.maxSummonMonsterLevel >= 9) {
    return summonAssistantChoicesNv9;
  }

  if (props.maxSummonMonsterLevel >= 8) {
    return summonAssistantChoicesNv8;
  }

  if (props.maxSummonMonsterLevel >= 7) {
    return summonAssistantChoicesNv7;
  }

  if (props.maxSummonMonsterLevel >= 6) {
    return summonAssistantChoicesNv6;
  }

  if (props.maxSummonMonsterLevel >= 5) {
    return summonAssistantChoicesNv5;
  }

  if (props.maxSummonMonsterLevel >= 4) {
    return summonAssistantChoicesNv4;
  }

  return summonAssistantChoicesNv3;
});

const suggestion = computed<SummonAssistantChoiceNv3 | SummonAssistantChoiceNv4 | SummonAssistantChoiceNv5 | SummonAssistantChoiceNv6 | SummonAssistantChoiceNv7 | SummonAssistantChoiceNv8 | SummonAssistantChoiceNv9 | null>(() => {
  if (!resolvedChoiceKey.value) {
    return null;
  }

  return activeChoiceTable.value[resolvedChoiceKey.value] ?? null;
});
const resolvedSuggestion = computed(() => {
  const choice = suggestion.value;
  const choiceKey = resolvedChoiceKey.value;

  if (!choice || !choiceKey) {
    return null;
  }

  const creatureType = combatStore.catalogItems.find(item => item.id === choice.creatureId)?.creatureType ?? null;

  return {
    ...choice,
    template: resolveSummonAssistantTemplate(choiceKey, creatureType, choice.template),
  };
});
const displayName = computed(() => resolvedSuggestion.value ? getSummonAssistantDisplayName(resolvedSuggestion.value.creatureId) : '');
const reasonEs = computed(() => resolvedSuggestion.value ? resolvedSuggestion.value.reason : '');
const currentTitle = computed(() => (isResolved.value ? 'Recomendación lista' : currentNode.value.title));

const choiceKeyByPath: Record<string, string> = {
  'Combate > Hacer daño > A un enemigo fuerte > CA alta': 'DAÑO_VS_CA_ALTA',
  'Combate > Hacer daño > A un enemigo fuerte > RD': 'DAÑO_VS_RD',
  'Combate > Hacer daño > A un enemigo fuerte > Muchos PG': 'DAÑO_VS_MUCHOS_PG',
  'Combate > Hacer daño > A un enemigo fuerte > Resistencia elemental': 'DAÑO_EVITAR_ELEMENTO_RESISTIDO',
  'Combate > Hacer daño > A un enemigo fuerte > No lo sé': 'DAÑO_GENERAL_SEGURO',
  'Combate > Hacer daño > A muchos enemigos débiles > Agrupados': 'DAÑO_MUCHOS_AGRUPADOS',
  'Combate > Hacer daño > A muchos enemigos débiles > Dispersos': 'DAÑO_MUCHOS_DISPERSOS',
  'Combate > Hacer daño > A muchos enemigos débiles > Mezclados con aliados': 'DAÑO_SELECTIVO_SIN_ESTORBAR_ALIADOS',
  'Combate > Hacer daño > A muchos enemigos débiles > No lo sé': 'DAÑO_MUCHOS_GENERAL',
  'Combate > Hacer daño > A un enemigo difícil de alcanzar > Vuela': 'DAÑO_ANTIAEREO',
  'Combate > Hacer daño > A un enemigo difícil de alcanzar > Está lejos': 'DAÑO_RAPIDO_A_DISTANCIA',
  'Combate > Hacer daño > A un enemigo difícil de alcanzar > Está tras enemigos': 'DAÑO_ATRAVESAR_LINEA_ENEMIGA',
  'Combate > Hacer daño > A un enemigo difícil de alcanzar > Está en altura': 'DAÑO_EN_ALTURA',
  'Combate > Hacer daño > A un enemigo difícil de alcanzar > Está en agua': 'DAÑO_ACUATICO',
  'Combate > Hacer daño > A un enemigo difícil de alcanzar > Está tras obstáculos': 'DAÑO_SUPERAR_OBSTACULOS',
  'Combate > Aguantar / tanquear > Un enemigo fuerte > Trabar': 'TANQUE_UN_ENEMIGO_TRABAR',
  'Combate > Aguantar / tanquear > Un enemigo fuerte > Sobrevivir': 'TANQUE_UN_ENEMIGO_RESISTIR',
  'Combate > Aguantar / tanquear > Un enemigo fuerte > Gastar acciones': 'TANQUE_UN_ENEMIGO_GASTAR_ACCIONES',
  'Combate > Aguantar / tanquear > Un enemigo fuerte > Proteger aliado': 'TANQUE_PROTEGER_ALIADO',
  'Combate > Aguantar / tanquear > Muchos enemigos > Ocupar casillas': 'TANQUE_MUCHOS_OCUPAR_CASILLAS',
  'Combate > Aguantar / tanquear > Muchos enemigos > Dividir ataques': 'TANQUE_MUCHOS_DIVIDIR_ATAQUES',
  'Combate > Aguantar / tanquear > Muchos enemigos > Cuello de botella': 'TANQUE_CUELLO_BOTELLA',
  'Combate > Aguantar / tanquear > Muchos enemigos > Aguantar ataques pequeños': 'TANQUE_MUCHOS_ATAQUES_PEQUEÑOS',
  'Combate > Aguantar / tanquear > Daño elemental > Fuego': 'TANQUE_VS_FUEGO',
  'Combate > Aguantar / tanquear > Daño elemental > Frío': 'TANQUE_VS_FRIO',
  'Combate > Aguantar / tanquear > Daño elemental > Ácido': 'TANQUE_VS_ACIDO',
  'Combate > Aguantar / tanquear > Daño elemental > Electricidad': 'TANQUE_VS_ELECTRICIDAD',
  'Combate > Aguantar / tanquear > Daño elemental > No lo sé': 'TANQUE_ELEMENTAL_GENERAL',
  'Combate > Bloquear o controlar espacio > Que pasen': 'BLOQUEO',
  'Combate > Bloquear o controlar espacio > Que carguen': 'ANTICARGA',
  'Combate > Bloquear o controlar espacio > Que lleguen a un aliado': 'PROTEGER',
  'Combate > Bloquear o controlar espacio > Que huyan': 'CORTAR_RETIRADA',
  'Combate > Bloquear o controlar espacio > Que rodeen': 'CONTROL_FLANCOS',
  'Combate > Cazar objetivo difícil > Volador': 'CAZAR_VOLADOR',
  'Combate > Cazar objetivo difícil > Lanzador': 'CAZAR_MAGO',
  'Combate > Cazar objetivo difícil > Arquero': 'CAZAR_ARQUERO',
  'Combate > Cazar objetivo difícil > Móvil': 'CAZAR_MOVIL',
  'Combate > Cazar objetivo difícil > Oculto': 'CAZAR_OCULTO',
  'Combate > Cazar objetivo difícil > Tras línea frontal': 'CAZAR_RETAGUARDIA',
  'Combate > Sobrevivir a una amenaza concreta > Fuego': 'RESISTIR_FUEGO',
  'Combate > Sobrevivir a una amenaza concreta > Frío': 'RESISTIR_FRIO',
  'Combate > Sobrevivir a una amenaza concreta > Ácido': 'RESISTIR_ACIDO',
  'Combate > Sobrevivir a una amenaza concreta > Electricidad': 'RESISTIR_ELECTRICIDAD',
  'Combate > Sobrevivir a una amenaza concreta > Veneno / enfermedad': 'RESISTIR_VENENO_ENFERMEDAD',
  'Combate > Sobrevivir a una amenaza concreta > Mente / miedo': 'RESISTIR_MENTE_MIEDO',
  'Combate > Sobrevivir a una amenaza concreta > Daño físico': 'RESISTIR_DAÑO_FISICO',
  'Combate > Sobrevivir a una amenaza concreta > Áreas': 'RESISTIR_AREAS',
  'Exploración > Buscar / rastrear': 'EXPLORAR',
  'Exploración > Zona peligrosa': 'EXPLORAR_RIESGO',
  'Exploración > Infiltrarse': 'INFILTRACION',
  'Exploración > Terreno especial': 'MOVILIDAD',
  'Exploración > Activar algo peligroso': 'ACTIVAR_RIESGO',
  'Exploración > Transportar': 'TRANSPORTE',
  'Huida / emergencia > Ganar tiempo': 'HUIDA_TIEMPO',
  'Huida / emergencia > Bloquear perseguidores': 'HUIDA_BLOQUEAR',
  'Huida / emergencia > Distraer': 'HUIDA_DISTRACCION',
  'Huida / emergencia > Rescatar / transportar': 'HUIDA_RESCATAR',
  'Huida / emergencia > Cubrir retirada': 'HUIDA_CUBRIR',
  'Huida / emergencia > Romper contacto': 'HUIDA_ROMPER_CONTACTO',
};

function resetAssistant(): void {
  nodeHistory.value = ['root'];
  path.value = [];
  resolvedChoiceKey.value = null;
}

function selectOption(option: SummonAssistantOption): void {
  if (option.nextNodeId) {
    path.value = [...path.value, option.label];
    nodeHistory.value = [...nodeHistory.value, option.nextNodeId];
    resolvedChoiceKey.value = null;
    return;
  }

  if (option.profileKey) {
    path.value = [...path.value, option.label];
    const choiceKey = choiceKeyByPath[path.value.join(' > ')] ?? null;
    resolvedChoiceKey.value = choiceKey;
  }
}

function handleBack(): void {
  if (resolvedChoiceKey.value) {
    resolvedChoiceKey.value = null;
    path.value = path.value.slice(0, -1);
    return;
  }

  if (nodeHistory.value.length <= 1) {
    return;
  }

  nodeHistory.value = nodeHistory.value.slice(0, -1);
  path.value = path.value.slice(0, -1);
}

function handleCancel(): void {
  emit('cancel');
}

function handleInvoke(): void {
  if (!resolvedSuggestion.value) {
    return;
  }

  emit('invoke', resolvedSuggestion.value);
}

function templateLabel(template: SummonTemplateType | null | undefined): string {
  if (!template) {
    return 'Sin plantilla';
  }

  const labels: Record<Exclude<SummonTemplateType, 'NONE'>, string> = {
    CHTHONIC: 'Chthonic',
    FIERY: 'Fiery',
    CELESTIAL: 'Celestial',
    ENTROPIC: 'Entropic',
    RESOLUTE: 'Resolute',
  };

  return labels[template];
}

watch(
  () => props.open,
  open => {
    if (open) {
      resetAssistant();
    }
  },
  { immediate: true },
);
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgba(2, 6, 23, 0.74);
}

.modal {
  width: min(920px, 100%);
  max-height: min(90vh, 920px);
  overflow: auto;
  background: rgba(15, 23, 42, 0.98);
  border: 1px solid rgba(51, 65, 85, 0.92);
  border-radius: 28px;
  box-shadow: 0 28px 60px rgba(0, 0, 0, 0.42);
  padding: 1rem;
}

.assistant-modal {
  display: grid;
  gap: 1rem;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: start;
}

.eyebrow,
.modal-header h2,
.question-panel h3,
.result-panel h3,
p {
  margin: 0;
}

.muted {
  color: #94a3b8;
  margin-top: 0.3rem;
}

.path-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.path-chip {
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  background: rgba(30, 41, 59, 0.75);
  border: 1px solid rgba(148, 163, 184, 0.16);
  color: #cbd5e1;
  font-size: 0.82rem;
}

.path-chip--empty {
  color: #94a3b8;
}

.question-panel,
.result-panel {
  display: grid;
  gap: 1rem;
}

.question-actions {
  display: flex;
  justify-content: flex-start;
}

.question-text {
  color: #cbd5e1;
}

.option-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.75rem;
}

.option-button {
  width: 100%;
}

.result-panel {
  padding: 1rem;
  border-radius: 1rem;
  background: rgba(30, 41, 59, 0.6);
}

.result-line {
  color: #e2e8f0;
}

.result-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  justify-content: flex-end;
}
</style>
