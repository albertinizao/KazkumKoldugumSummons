import { defineStore } from 'pinia'
import { creatureCatalog } from '@/data/catalog'
import { getSummonQuantity } from '@/services/api'
import {
  decreaseDailyUses as decreaseDailyUsesApi,
  fetchConfiguration,
  increaseDailyUses as increaseDailyUsesApi,
  resetDailyUses as resetDailyUsesApi,
} from '@/services/combatApi'
import { readJson, writeJson } from '@/utils/persistence'
import { resolveCreature, rollQuantity } from '@/utils/summoning'

const STORAGE_KEY = 'kazkum.combat.v1'

function createInstance(resolvedCreature, ordinal) {
  return {
    id: crypto.randomUUID(),
    ordinal,
    label: `${resolvedCreature.name} #${ordinal}`,
    currentHitPoints: resolvedCreature.defenses.hp,
    maxHitPoints: resolvedCreature.defenses.hp,
    state: 'HEALTHY'
  }
}

function deriveState(hitPoints, maxHitPoints) {
  if (hitPoints <= 0) return 'DOWN'
  if (hitPoints < maxHitPoints) return 'DAMAGED'
  return 'HEALTHY'
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value))
}

export const useCombatStore = defineStore('combat', {
  state: () => ({
    dailyUses: { maximum: 6, remaining: 4 },
    dailyUsesLoading: false,
    dailyUsesError: null,
    groups: [],
    recentSummons: [],
    popularSummons: [],
    summonModalOpen: false,
    selectedCreatureId: creatureCatalog[0]?.id ?? null,
    selectedTemplateKey: 'none',
    summonPreview: null,
    pendingQuantity: null,
    statusMessage: ''
  }),
  getters: {
    selectedCreature() {
      return creatureCatalog.find(creature => creature.id === this.selectedCreatureId) ?? null
    },
    activeInstanceCount() {
      return this.groups.reduce((total, group) => total + group.instances.length, 0)
    }
  },
  actions: {
    hydrate() {
      const persisted = readJson(STORAGE_KEY, null)
      if (persisted) {
        this.dailyUses = persisted.dailyUses ?? this.dailyUses
        this.groups = persisted.groups ?? []
        this.recentSummons = persisted.recentSummons ?? []
        this.popularSummons = persisted.popularSummons ?? []
      }
      this.normalizeDailyUses()
      this.recomputePopularSummons()
      this.persist()
    },
    persist() {
      writeJson(STORAGE_KEY, {
        dailyUses: this.dailyUses,
        groups: this.groups,
        recentSummons: this.recentSummons,
        popularSummons: this.popularSummons
      })
    },
    normalizeDailyUses() {
      this.dailyUses.maximum = Math.max(0, Number(this.dailyUses.maximum ?? 0))
      this.dailyUses.remaining = clamp(Number(this.dailyUses.remaining ?? 0), 0, this.dailyUses.maximum)
    },
    async loadDailyUses() {
      this.dailyUsesLoading = true
      this.dailyUsesError = null
      try {
        const configuration = await fetchConfiguration()
        this.dailyUses = {
          maximum: Math.max(0, Number(configuration.dailyUses.maximum ?? 0)),
          remaining: clamp(Number(configuration.dailyUses.remaining ?? 0), 0, Math.max(0, Number(configuration.dailyUses.maximum ?? 0)))
        }
        this.persist()
      } catch (error) {
        this.dailyUsesError = error instanceof Error ? error.message : String(error)
      } finally {
        this.dailyUsesLoading = false
      }
    },
    setDailyUsesMaximum(value) {
      this.dailyUses.maximum = Math.max(0, Number(value))
      this.dailyUses.remaining = clamp(this.dailyUses.remaining, 0, this.dailyUses.maximum)
      this.persist()
    },
    setDailyUsesRemaining(value) {
      this.dailyUses.remaining = clamp(Number(value), 0, this.dailyUses.maximum)
      this.persist()
    },
    async incrementDailyUses(amount = 1) {
      this.dailyUsesLoading = true
      this.dailyUsesError = null
      try {
        const response = await increaseDailyUsesApi(amount)
        this.dailyUses = response.dailyUses
        this.persist()
        return response
      } catch (error) {
        this.dailyUsesError = error instanceof Error ? error.message : String(error)
        throw error
      } finally {
        this.dailyUsesLoading = false
      }
    },
    async decrementDailyUses(amount = 1) {
      this.dailyUsesLoading = true
      this.dailyUsesError = null
      try {
        const response = await decreaseDailyUsesApi(amount)
        this.dailyUses = response.dailyUses
        this.persist()
        return response
      } catch (error) {
        this.dailyUsesError = error instanceof Error ? error.message : String(error)
        throw error
      } finally {
        this.dailyUsesLoading = false
      }
    },
    async resetDailyUses() {
      this.dailyUsesLoading = true
      this.dailyUsesError = null
      try {
        const response = await resetDailyUsesApi()
        this.dailyUses = response.dailyUses
        this.persist()
        return response
      } catch (error) {
        this.dailyUsesError = error instanceof Error ? error.message : String(error)
        throw error
      } finally {
        this.dailyUsesLoading = false
      }
    },
    openSummonModal(creature = null, templateKey = 'none') {
      this.selectedCreatureId = creature?.id ?? this.selectedCreatureId
      const allowedTemplate = creature?.allowedTemplates?.[0] ?? 'none'
      this.selectedTemplateKey = templateKey !== 'none'
        ? templateKey
        : allowedTemplate !== 'none'
          ? allowedTemplate
          : 'none'
      this.summonModalOpen = true
      this.preparePreview()
    },
    closeSummonModal() {
      this.summonModalOpen = false
      this.pendingQuantity = null
      this.summonPreview = null
    },
    setSelectedTemplateKey(templateKey) {
      this.selectedTemplateKey = templateKey
      this.preparePreview()
    },
    async preparePreview() {
      const creature = this.selectedCreature
      if (!creature) {
        this.summonPreview = null
        return
      }

      const quantityInfo = await getSummonQuantity(creature.summonLevel)
      this.pendingQuantity = {
        formula: quantityInfo.formula,
        maximumPossibleQuantity: quantityInfo.maximumPossibleQuantity,
        available: quantityInfo.available,
        rolledQuantity: rollQuantity(quantityInfo.formula)
      }
      this.summonPreview = resolveCreature(creature, this.selectedTemplateKey)
    },
    async summonSelectedCreature() {
      const creature = this.selectedCreature
      if (!creature) return null

      const quantityInfo = await getSummonQuantity(creature.summonLevel)
      if (!quantityInfo.available) {
        this.statusMessage = 'La criatura seleccionada excede el nivel máximo configurado.'
        return null
      }

      const quantity = this.pendingQuantity?.rolledQuantity ?? rollQuantity(quantityInfo.formula)
      const resolvedCreature = resolveCreature(creature, this.selectedTemplateKey)
      const groupIdentity = resolvedCreature.identity
      const existingGroup = this.groups.find(group => group.identity === groupIdentity)

      const instances = Array.from({ length: quantity }, (_, index) => createInstance(resolvedCreature, index + 1))

      if (existingGroup) {
        const start = existingGroup.instances.length
        existingGroup.instances.push(...instances.map((instance, index) => ({
          ...instance,
          ordinal: start + index + 1,
          label: `${resolvedCreature.name} #${start + index + 1}`
        })))
        existingGroup.quantity = existingGroup.instances.length
      } else {
        this.groups.unshift({
          identity: groupIdentity,
          baseCreatureId: creature.id,
          resolvedCreature,
          quantity,
          quantityFormula: quantityInfo.formula,
          templateKey: this.selectedTemplateKey,
          label: resolvedCreature.name,
          instances
        })
      }

      this.dailyUses.remaining = clamp(this.dailyUses.remaining - 1, 0, this.dailyUses.maximum)
      this.recordSummon({
        identity: groupIdentity,
        creatureId: creature.id,
        creatureName: creature.name,
        templateKey: this.selectedTemplateKey,
        templateLabel: resolvedCreature.templateLabel,
        createdAt: new Date().toISOString()
      })

      this.statusMessage = `${resolvedCreature.name} invocado con ${quantity} instancia(s).`
      this.persist()
      this.closeSummonModal()
      return quantity
    },
    recordSummon(entry) {
      this.recentSummons = [entry, ...this.recentSummons.filter(item => item.identity !== entry.identity)].slice(0, 8)
      const existing = this.popularSummons.find(item => item.identity === entry.identity)
      if (existing) {
        existing.count += 1
        existing.lastSummonedAt = entry.createdAt
      } else {
        this.popularSummons.unshift({
          identity: entry.identity,
          creatureId: entry.creatureId,
          creatureName: entry.creatureName,
          templateKey: entry.templateKey,
          templateLabel: entry.templateLabel,
          count: 1,
          lastSummonedAt: entry.createdAt
        })
      }
      this.recomputePopularSummons()
      this.persist()
    },
    recomputePopularSummons() {
      this.popularSummons = [...this.popularSummons].sort((a, b) => b.count - a.count || new Date(b.lastSummonedAt) - new Date(a.lastSummonedAt))
    },
    setInstanceHitPoints(groupIdentity, instanceId, nextHitPoints) {
      const group = this.groups.find(item => item.identity === groupIdentity)
      if (!group) return
      const instance = group.instances.find(item => item.id === instanceId)
      if (!instance) return

      instance.currentHitPoints = clamp(nextHitPoints, 0, instance.maxHitPoints)
      instance.state = deriveState(instance.currentHitPoints, instance.maxHitPoints)
      this.persist()
    },
    damageInstance(groupIdentity, instanceId, amount = 1) {
      const group = this.groups.find(item => item.identity === groupIdentity)
      const instance = group?.instances.find(item => item.id === instanceId)
      if (!instance) return
      this.setInstanceHitPoints(groupIdentity, instanceId, instance.currentHitPoints - Math.max(1, amount))
    },
    healInstance(groupIdentity, instanceId, amount = 1) {
      const group = this.groups.find(item => item.identity === groupIdentity)
      const instance = group?.instances.find(item => item.id === instanceId)
      if (!instance) return
      this.setInstanceHitPoints(groupIdentity, instanceId, instance.currentHitPoints + Math.max(1, amount))
    },
    removeInstance(groupIdentity, instanceId) {
      const index = this.groups.findIndex(item => item.identity === groupIdentity)
      if (index < 0) return
      const group = this.groups[index]
      group.instances = group.instances.filter(instance => instance.id !== instanceId)
      group.quantity = group.instances.length
      if (group.instances.length === 0) {
        this.groups.splice(index, 1)
      }
      this.persist()
    },
    clearAllGroups() {
      this.groups = []
      this.persist()
    }
  }
})
