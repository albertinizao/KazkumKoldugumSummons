import { createRouter, createWebHistory } from 'vue-router'
import CombatView from '@/views/CombatView.vue'
import CatalogView from '@/views/CatalogView.vue'
import SettingsView from '@/views/SettingsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'combat', component: CombatView },
    { path: '/catalogo', name: 'catalogo', component: CatalogView },
    { path: '/configuracion', name: 'configuracion', component: SettingsView }
  ]
})

export default router
