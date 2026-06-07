import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'combat',
      component: () => import('@/views/CombatView.vue'),
    },
    {
      path: '/catalogo',
      name: 'catalogo',
      component: () => import('@/views/CatalogView.vue'),
    },
    {
      path: '/configuracion',
      name: 'configuracion',
      component: () => import('@/views/SettingsView.vue'),
    },
  ],
});

export default router;
