import { defineStore } from 'pinia'
import { getConfiguration, saveConfiguration } from '@/services/api'

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    maxSummonMonsterLevel: 3,
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
        const configuration = await getConfiguration()
        this.maxSummonMonsterLevel = configuration.maxSummonMonsterLevel
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
