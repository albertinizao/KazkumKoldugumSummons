<template>
  <section class="empty-state">
    <h3>{{ title }}</h3>
    <p class="muted">{{ description }}</p>
  </section>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, required: true }
})
</script>
