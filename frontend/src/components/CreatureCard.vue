<template>
  <article class="card">
    <div class="section-title">
      <div>
        <p class="eyebrow">Nivel {{ creature.summonLevel }}</p>
        <h3>{{ creature.name }}</h3>
      </div>
      <span class="chip accent">{{ isAvailable ? 'Disponible' : 'Bloqueada' }}</span>
    </div>

    <div class="chips">
      <span class="chip">{{ creature.size }}</span>
      <span class="chip">{{ creature.type }}</span>
      <span class="chip" v-if="creature.subtype">{{ creature.subtype }}</span>
      <span class="chip">{{ creature.alignment }}</span>
    </div>

    <div class="divider"></div>

    <div class="meta small">
      <div class="meta-item">
        <span>CA</span>
        <strong>{{ creature.defenses.ac }}</strong>
      </div>
      <div class="meta-item">
        <span>PG</span>
        <strong>{{ creature.defenses.hp }}</strong>
      </div>
      <div class="meta-item">
        <span>Ataques</span>
        <strong>{{ creature.attacks.length }}</strong>
      </div>
    </div>

    <div class="divider"></div>

    <div class="stack small">
      <div>
        <strong>Velocidades</strong>
        <p class="muted">{{ speedSummary }}</p>
      </div>
      <div>
        <strong>Plantillas</strong>
        <p class="muted">{{ templateSummary }}</p>
      </div>
    </div>

    <div class="divider"></div>

    <div class="row-actions">
      <button class="secondary" @click="$emit('preview', creature)">Ver vista previa</button>
      <button @click="$emit('summon', creature)">Invocar</button>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  creature: { type: Object, required: true },
  maxSummonMonsterLevel: { type: Number, required: true }
})

defineEmits(['preview', 'summon'])

const isAvailable = computed(() => props.creature.summonLevel <= props.maxSummonMonsterLevel)
const speedSummary = computed(() => props.creature.speeds.map(speed => `${speed.type} ${speed.value} ft`).join(' · '))
const templateSummary = computed(() => props.creature.allowedTemplates.join(', '))
</script>
