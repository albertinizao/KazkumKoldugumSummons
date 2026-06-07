import { summonTemplates } from '@/data/catalog'

const TEMPLATE_MAP = Object.fromEntries(summonTemplates.map(template => [template.key, template]))

function highestSpeed(creature) {
  return creature.speeds.reduce((max, speed) => Math.max(max, speed.value), 0)
}

export function getTemplate(templateKey) {
  return TEMPLATE_MAP[templateKey] ?? TEMPLATE_MAP.none
}

export function resolveCreature(creature, templateKey = 'none') {
  const template = getTemplate(templateKey)
  const isTemplate = templateKey !== 'none'
  const hasEarthSubtype = creature.subtype?.toLowerCase().includes('earth') || templateKey === 'chthonic'
  const hasBurrow = creature.speeds.some(speed => speed.type === 'burrow') || templateKey === 'chthonic'
  const finalName = isTemplate ? `${template.label} ${creature.name}` : creature.name
  const speedLines = creature.speeds.map(speed => `${speed.type} ${speed.value} ft`)
  const notes = [...(creature.notes ? [creature.notes] : [])]

  const traits = new Set([creature.type, creature.size])
  if (creature.subtype) traits.add(creature.subtype)
  if (isTemplate) traits.add(template.label)
  if (hasEarthSubtype) traits.add('Earth synergy')
  if (hasBurrow) traits.add('Burrow')

  const attackBonus = hasEarthSubtype || hasBurrow ? 1 : 0
  const armorBonus = hasEarthSubtype || hasBurrow ? 1 : 0
  const damageBonus = templateKey === 'chthonic'
    ? 'All attacks gain +1 acid damage'
    : templateKey === 'fiery'
      ? 'All attacks gain +1 fire damage'
      : null

  if (templateKey === 'chthonic') {
    notes.push('Alignment changes to NG.')
    notes.push('Adds earth subtype, darkvision 60 ft and tremorsense 60 ft.')
    const burrowValue = Math.max(5, Math.floor(highestSpeed(creature) / 2))
    notes.push(`Adds burrow speed ${burrowValue} ft based on the highest existing speed.`)
    notes.push('Adds acid resistance 10 and listed immunities.')
  } else if (templateKey === 'fiery') {
    notes.push('Alignment changes to NG.')
    notes.push('Adds fire subtype and darkvision 60 ft.')
    notes.push('Adds fire resistance 10.')
  } else if (templateKey === 'celestial' || templateKey === 'entropic' || templateKey === 'resolute') {
    notes.push('Alignment changes to NG.')
    notes.push('Adds darkvision 60 ft and the template-listed resistances.')
    notes.push(`Adds a single-use swift-action smite ${templateKey === 'celestial' ? 'evil' : templateKey === 'entropic' ? 'good' : 'chaos'} attack.`)
  }

  if (hasEarthSubtype || hasBurrow) {
    notes.push('Earth or burrow synergy grants +1 attack and +1 AC normal/flat-footed.')
  }

  return {
    identity: `${creature.id}::${templateKey}`,
    name: finalName,
    templateKey,
    templateLabel: template.label,
    finalAlignment: isTemplate ? 'NG' : creature.alignment,
    finalSubtype: [
      creature.subtype,
      templateKey === 'chthonic' ? 'Earth' : null,
      templateKey === 'fiery' ? 'Fire' : null
    ].filter(Boolean).join(', ') || creature.type,
    speedLines,
    attacks: creature.attacks.map(attack => ({
      ...attack,
      bonus: attack.bonus + attackBonus,
      damage: damageBonus ? `${attack.damage} (+1 ${templateKey === 'chthonic' ? 'acid' : 'fire'})` : attack.damage
    })),
    defenses: {
      ...creature.defenses,
      ac: creature.defenses.ac + armorBonus,
      touch: creature.defenses.ac + armorBonus,
      flatFooted: creature.defenses.ac + armorBonus
    },
    traits: Array.from(traits),
    notes,
    isEarthSynergy: hasEarthSubtype || hasBurrow,
    burrowSpeed: templateKey === 'chthonic' ? Math.max(5, Math.floor(highestSpeed(creature) / 2)) : creature.speeds.find(speed => speed.type === 'burrow')?.value ?? null
  }
}

export function rollQuantity(formula) {
  if (formula === '1') return 1
  const match = /^1d(\d+)(?:\+(\d+))?$/.exec(formula)
  if (!match) return 1

  const sides = Number(match[1])
  const bonus = Number(match[2] ?? 0)
  return 1 + Math.floor(Math.random() * sides) + bonus
}
