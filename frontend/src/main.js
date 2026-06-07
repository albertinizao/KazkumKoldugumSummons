import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles.css'
import { useCombatStore } from './stores/combat'
import { useSettingsStore } from './stores/settings'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const combatStore = useCombatStore(pinia)
const settingsStore = useSettingsStore(pinia)

if (typeof window !== 'undefined') {
  combatStore.hydrate()
  settingsStore.loadConfiguration().catch(() => {})
}

app.mount('#app')
