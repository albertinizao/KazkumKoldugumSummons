<template>
  <button class="action-button" type="button">
    <slot />
  </button>
</template>

<style scoped>
.action-button {
  min-height: 3rem;
  padding: 0.8rem 1rem;
  border-radius: 0.9rem;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: linear-gradient(180deg, rgba(51, 65, 85, 0.9), rgba(30, 41, 59, 0.95));
  color: #f8fafc;
  font-weight: 700;
  cursor: pointer;
}

.action-button:hover {
  border-color: rgba(129, 140, 248, 0.55);
}
</style>
