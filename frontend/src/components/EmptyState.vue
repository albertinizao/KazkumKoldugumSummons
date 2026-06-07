<template>
  <section class="empty-state">
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
}>();
</script>

<style scoped>
.empty-state {
  padding: 1.5rem;
  border-radius: 1rem;
  border: 1px dashed rgba(148, 163, 184, 0.3);
  background: rgba(15, 23, 42, 0.5);
}

h2,
p {
  margin: 0;
}

p {
  margin-top: 0.5rem;
  color: #94a3b8;
}
</style>
