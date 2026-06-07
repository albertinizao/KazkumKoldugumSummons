<template>
  <header class="app-header">
    <div class="brand">
      <p class="eyebrow">Pathfinder 1e · Inquisidor Monster Tactician</p>
      <h1>Kazkum Koldugum Summons</h1>
      <p class="muted">Gestor de invocaciones, catálogo y configuración persistente.</p>
    </div>
    <nav class="nav" aria-label="Navegación principal">
      <RouterLink to="/">Combate</RouterLink>
      <RouterLink to="/catalogo">Catálogo</RouterLink>
      <RouterLink to="/configuracion">Configuración</RouterLink>
    </nav>
  </header>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>
