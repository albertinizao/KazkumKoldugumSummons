<template>
  <header class="topbar">
    <div>
      <p class="eyebrow">Pathfinder 1e · Inquisidor Monster Tactician</p>
      <h1>Kazkum Koldugum Summons</h1>
    </div>

    <nav class="topbar-nav" aria-label="Navegación principal">
      <RouterLink to="/">Combate</RouterLink>
      <RouterLink to="/catalogo">Catálogo</RouterLink>
      <RouterLink to="/configuracion">Configuración</RouterLink>
    </nav>
  </header>
</template>

<style scoped>
.topbar {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(10, 14, 22, 0.8);
  backdrop-filter: blur(12px);
}

.eyebrow {
  margin: 0 0 0.25rem;
  color: #94a3b8;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  font-size: 1.25rem;
}

.topbar-nav {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.topbar-nav a {
  text-decoration: none;
  padding: 0.55rem 0.8rem;
  border-radius: 0.75rem;
  background: rgba(30, 41, 59, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.15);
}

.topbar-nav a.router-link-active {
  background: rgba(79, 70, 229, 0.35);
  border-color: rgba(129, 140, 248, 0.45);
}

@media (max-width: 720px) {
  .topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .topbar-nav {
    justify-content: space-between;
  }
}
</style>
