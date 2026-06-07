<template>
  <article class="card">
    <div class="section-title">
      <div>
        <p class="eyebrow">Grupo activo</p>
        <h3>{{ group.label }}</h3>
        <p class="muted">{{ group.quantity }} instancia(s) · {{ group.quantityFormula }}</p>
      </div>
      <span class="chip success">{{ group.resolvedCreature.templateLabel }}</span>
    </div>

    <div class="meta">
      <div class="meta-item">
        <span>CA</span>
        <strong>{{ group.resolvedCreature.defenses.ac }}</strong>
      </div>
      <div class="meta-item">
        <span>PG</span>
        <strong>{{ group.resolvedCreature.defenses.hp }}</strong>
      </div>
      <div class="meta-item">
        <span>Alcance</span>
        <strong>{{ attackCount }}</strong>
      </div>
      <div class="meta-item">
        <span>Identidad final</span>
        <strong>{{ group.resolvedCreature.identity }}</strong>
      </div>
    </div>

    <div class="divider"></div>

    <div class="stack small">
      <div>
        <strong>Atributos finales</strong>
        <p class="muted">{{ group.resolvedCreature.finalAlignment }} · {{ group.resolvedCreature.finalSubtype }}</p>
      </div>
      <div>
        <strong>Velocidades</strong>
        <p class="muted">{{ group.resolvedCreature.speedLines.join(' · ') }}</p>
      </div>
      <div>
        <strong>Notas</strong>
        <ul class="muted">
          <li v-for="note in group.resolvedCreature.notes" :key="note">{{ note }}</li>
        </ul>
      </div>
    </div>

    <div class="divider"></div>

    <div class="stack">
      <div v-for="instance in group.instances" :key="instance.id" class="table-row compact">
        <div>
          <strong>{{ instance.label }}</strong>
          <div class="muted small">{{ instance.state }} · {{ instance.currentHitPoints }} / {{ instance.maxHitPoints }} PG</div>
        </div>
        <div class="chips">
          <span class="chip" :class="instance.state.toLowerCase()">{{ instance.state }}</span>
        </div>
        <div class="row-actions">
          <button class="secondary" @click="$emit('damage', group.identity, instance.id, 1)">-1</button>
          <button class="secondary" @click="$emit('damage', group.identity, instance.id, 5)">-5</button>
          <button class="secondary" @click="$emit('heal', group.identity, instance.id, 1)">+1</button>
          <button class="secondary" @click="$emit('heal', group.identity, instance.id, 5)">+5</button>
          <button class="danger" @click="$emit('remove', group.identity, instance.id)">Eliminar</button>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

defineEmits(['damage', 'heal', 'remove'])

const props = defineProps({
  group: { type: Object, required: true }
})

const attackCount = computed(() => props.group.resolvedCreature.attacks.length)
</script>
