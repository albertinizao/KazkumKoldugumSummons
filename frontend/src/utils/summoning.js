import { summonTemplates } from '@/data/catalog'

const TEMPLATE_MAP = Object.fromEntries(summonTemplates.map(template => [template.key, template]))

function highestSpeed(creature) {
  return creature.speeds.reduce((max, speed) => Math.max(max, speed.value), 0)
}

function appendUnique(values, value) {
  if (!values.includes(value)) {
    values.push(value)
  }
}

function hasEarthSubtype(creature) {
  return creature.subtype?.toLowerCase().includes('earth') ?? false
}

function hasBurrowSpeed(creature) {
  return creature.speeds.some(speed => speed.type === 'burrow')
}

function templateSpecialAttack(templateKey) {
  if (templateKey === 'celestial') return 'Smite evil 1/day (swift action)'
  if (templateKey === 'entropic') return 'Smite law 1/day (swift action)'
  if (templateKey === 'resolute') return 'Smite chaos 1/day (swift action)'
  return null
}

export function getTemplate(templateKey) {
  return TEMPLATE_MAP[templateKey] ?? TEMPLATE_MAP.none
}

export function resolveCreature(creature, templateKey = 'none') {
  const template = getTemplate(templateKey)
  const isTemplate = templateKey !== 'none'
  const templateApplied = isTemplate && template.key !== 'none'
  const hasEarth = hasEarthSubtype(creature) || templateKey === 'chthonic'
  const hasBurrow = hasBurrowSpeed(creature) || templateKey === 'chthonic'
  const finalName = isTemplate ? `${template.label} ${creature.name}` : creature.name
  const resolvedSpeeds = creature.speeds.map(speed => ({ ...speed }))
  if (templateKey === 'chthonic' && !hasBurrowSpeed(creature)) {
    resolvedSpeeds.push({
      type: 'burrow',
      value: Math.max(5, Math.floor(highestSpeed(creature) / 2)),
    })
  }
  const speedLines = resolvedSpeeds.map(speed => `${speed.type} ${speed.value} ft`)
  const notes = [...(creature.notes ? [creature.notes] : [])]
  const specialAttacks = [...(creature.specialAttacks ?? [])]
  const specialDefenses = [...(creature.specialDefenses ?? [])]

  const traits = new Set([creature.type, creature.size])
  if (creature.subtype) traits.add(creature.subtype)
  if (isTemplate) traits.add(template.label)
  if (hasEarth) traits.add('Earth synergy')
  if (hasBurrow) traits.add('Burrow')

  const deepGuardianBonus = hasEarth || hasBurrow ? 1 : 0
  const templateDamage = templateKey === 'chthonic'
    ? { type: 'acid', label: 'acid' }
    : templateKey === 'fiery'
      ? { type: 'fire', label: 'fire' }
      : null

  if (templateApplied) {
    notes.push('Alignment changes to NG.')
  }

  if (templateKey === 'chthonic') {
    notes.push('Adds earth subtype, darkvision 60 ft and tremorsense 60 ft.')
    const burrowValue = Math.max(5, Math.floor(highestSpeed(creature) / 2))
    notes.push(`Adds burrow speed ${burrowValue} ft based on the highest existing speed.`)
    specialDefenses.push('Resistance acid 10')
    specialDefenses.push('Listed immunities')
  } else if (templateKey === 'fiery') {
    notes.push('Adds fire subtype and darkvision 60 ft.')
    specialDefenses.push('Resistance fire 10')
    specialDefenses.push('Immunity fire')
    specialDefenses.push('Vulnerability cold')
  } else if (templateKey === 'celestial' || templateKey === 'entropic' || templateKey === 'resolute') {
    notes.push('Adds darkvision 60 ft and the template-listed resistances.')
    const smite = templateSpecialAttack(templateKey)
    if (smite) {
      specialAttacks.push(smite)
    }
    if (templateKey === 'celestial') {
      specialDefenses.push('Resistance cold 10')
      specialDefenses.push('Resistance acid 10')
      specialDefenses.push('Resistance electricity 10')
    } else if (templateKey === 'entropic') {
      specialDefenses.push('Resistance acid 10')
      specialDefenses.push('Resistance fire 10')
    } else if (templateKey === 'resolute') {
      specialDefenses.push('Resistance acid 10')
      specialDefenses.push('Resistance cold 10')
      specialDefenses.push('Resistance fire 10')
    }
  }

  if (hasEarth || hasBurrow) {
    notes.push('Earth or burrow synergy grants +1 attack and +1 AC normal/flat-footed.')
  }

  return {
    identity: `${creature.id}::${templateKey}`,
    name: finalName,
    templateKey,
    templateLabel: template.label,
    finalAlignment: templateApplied ? 'NG' : creature.alignment,
    finalSubtype: Array.from(new Set([
      creature.subtype,
      templateKey === 'chthonic' ? 'Earth' : null,
      templateKey === 'fiery' ? 'Fire' : null
    ].filter(Boolean))).join(', ') || creature.type,
    speedLines,
    attacks: creature.attacks.map(attack => ({
      ...attack,
      bonus: attack.bonus + deepGuardianBonus,
      damage: templateDamage ? `${attack.damage} (+1 ${templateDamage.label})` : attack.damage
    })),
    defenses: {
      ...creature.defenses,
      ac: creature.defenses.ac + deepGuardianBonus,
      touch: creature.defenses.ac,
      flatFooted: creature.defenses.ac + deepGuardianBonus
    },
    traits: Array.from(traits),
    notes,
    specialAttacks,
    specialDefenses,
    isEarthSynergy: hasEarth || hasBurrow,
    burrowSpeed: templateKey === 'chthonic'
      ? Math.max(5, Math.floor(highestSpeed(creature) / 2))
      : creature.speeds.find(speed => speed.type === 'burrow')?.value ?? null
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
