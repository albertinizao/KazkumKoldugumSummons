<template>
  <div class="app-shell">
    <AppHeader />
    <main class="app-main">
      <router-view />
    </main>
    <SummonModal />
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
import SummonModal from '@/components/SummonModal.vue'
</script>
