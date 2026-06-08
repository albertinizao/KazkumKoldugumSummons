import { defineStore } from 'pinia'
import { fetchConfiguration } from '@/services/combatApi'
import { saveConfiguration } from '@/services/api'

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    maxSummonMonsterLevel: 3,
    dailyUses: { maximum: 6, remaining: 4 },
    loading: false,
    saving: false,
    loaded: false,
    error: null,
    lastSavedAt: null
  }),
  getters: {
    displayMaxSummonMonsterLevel: state => state.maxSummonMonsterLevel
  },
  actions: {
    async loadConfiguration() {
      this.loading = true
      this.error = null
      try {
        const configuration = await fetchConfiguration()
        this.maxSummonMonsterLevel = configuration.maxSummonMonsterLevel
        this.dailyUses = configuration.dailyUses
        this.loaded = true
      } catch (error) {
        this.error = error instanceof Error ? error.message : String(error)
      } finally {
        this.loading = false
      }
    },
    async updateMaxSummonMonsterLevel(value) {
      this.saving = true
      this.error = null
      try {
        const configuration = await saveConfiguration(value)
        this.maxSummonMonsterLevel = configuration.maxSummonMonsterLevel
        this.loaded = true
        this.lastSavedAt = new Date().toISOString()
        return configuration
      } catch (error) {
        this.error = error instanceof Error ? error.message : String(error)
        throw error
      } finally {
        this.saving = false
      }
    }
  }
})
