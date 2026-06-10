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
          <h3>{{ suggestion?.variant.label }}</h3>
          <p class="result-line">
            <strong>Perfil:</strong> {{ suggestion?.profileTitle }}
          </p>
          <p class="result-line">
            <strong>Nivel:</strong> {{ suggestion?.variant.summonLevel }}
          </p>
          <p class="result-line">
            <strong>Criatura:</strong> {{ suggestion?.variant.creatureName }}
          </p>
          <p class="result-line">
            <strong>Plantilla:</strong> {{ templateLabel(suggestion?.variant.template) }}
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
import {
  getSummonAssistantSuggestion,
  summonAssistantNodes,
  type SummonAssistantNode,
  type SummonAssistantOption,
  type SummonAssistantProfileKey,
  type SummonAssistantSuggestion,
} from '@/data/summonAssistant';
import type { SummonTemplateType } from '@/types/catalog';

const props = defineProps<{
  open: boolean;
  maxSummonMonsterLevel: number;
}>();

const emit = defineEmits<{
  cancel: [];
  invoke: [suggestion: SummonAssistantSuggestion];
}>();

const titleId = 'summon-assistant-title';
const nodeHistory = ref<string[]>(['root']);
const path = ref<string[]>([]);
const resolvedProfileKey = ref<SummonAssistantProfileKey | null>(null);

const currentNodeId = computed(() => nodeHistory.value[nodeHistory.value.length - 1] ?? 'root');
const currentNode = computed<SummonAssistantNode>(() => summonAssistantNodes[currentNodeId.value] ?? summonAssistantNodes.root);
const isResolved = computed(() => resolvedProfileKey.value !== null);
const canGoBack = computed(() => nodeHistory.value.length > 1 || path.value.length > 0);
const suggestion = computed(() => {
  if (!resolvedProfileKey.value) {
    return null;
  }

  return getSummonAssistantSuggestion(resolvedProfileKey.value, props.maxSummonMonsterLevel);
});
const currentTitle = computed(() => (isResolved.value ? 'Recomendación lista' : currentNode.value.title));

function resetAssistant(): void {
  nodeHistory.value = ['root'];
  path.value = [];
  resolvedProfileKey.value = null;
}

function selectOption(option: SummonAssistantOption): void {
  if (option.nextNodeId) {
    path.value = [...path.value, option.label];
    nodeHistory.value = [...nodeHistory.value, option.nextNodeId];
    resolvedProfileKey.value = null;
    return;
  }

  if (option.profileKey) {
    path.value = [...path.value, option.label];
    resolvedProfileKey.value = option.profileKey;
  }
}

function handleBack(): void {
  if (resolvedProfileKey.value) {
    resolvedProfileKey.value = null;
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
  if (!suggestion.value) {
    return;
  }

  emit('invoke', suggestion.value);
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
