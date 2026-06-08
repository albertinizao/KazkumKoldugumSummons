# Modelo de dominio

Este documento define el modelo de dominio funcional de la aplicación.

El objetivo es describir las entidades, value objects, enumeraciones y reglas principales que deben existir en la lógica de dominio, independientemente de cómo se persistan o se expongan por API REST.

La aplicación debe mantener una separación clara entre:

- datos base de criaturas;
- criatura final transformada;
- invocaciones activas;
- instancias individuales con PG propios;
- tiradas;
- reglas fijas del personaje.

---

# Principios del modelo

## 1. Pathfinder 1e

El modelo está diseñado para Pathfinder 1e.

No debe incorporar conceptos de Pathfinder 2e, D&D 5e ni Starfinder.

## 2. Criatura base y criatura final son conceptos distintos

Una criatura base procede del catálogo JSON.

Una criatura final es el resultado de aplicar reglas fijas del personaje y, si procede, una plantilla.

La pantalla principal, las tiradas y las instancias activas deben trabajar con la criatura final.

## 3. Las criaturas activas tienen PG independientes

Varias criaturas del mismo tipo comparten ficha final, pero cada instancia tiene sus propios PG actuales.

Ejemplo:

```text
Fiery Badger x3

Fiery Badger 1 — PG 15 / 15
Fiery Badger 2 — PG 7 / 15
Fiery Badger 3 — PG -2 / 15
```

## 4. El dominio no debe resolver reglas contextuales

El dominio puede tirar dados y aplicar reglas fijas.

No debe decidir:

- si un ataque impacta;
- si un crítico confirma contra la CA enemiga;
- si una criatura puede actuar;
- si se aplica RD, resistencia o inmunidad;
- consecuencias de estados;
- modificadores temporales.

---

# Entidades principales

## CreatureTemplate

Representa la ficha base de una criatura invocable cargada desde JSON.

Es la fuente original de datos antes de aplicar reglas fijas del personaje.

No representa una criatura activa en combate.

### Campos

```text
id: CreatureTemplateId
name: String
summonLevel: SummonLevel
alignment: Alignment
size: CreatureSize
creatureType: CreatureType
subtypes: List<CreatureSubtype>
allowedTemplates: List<SummonTemplateType>
initiative: Modifier
senses: List<Sense>
perception: Modifier
abilities: AbilityScores
armorClass: ArmorClass
hitPoints: HitPointsDefinition
savingThrows: SavingThrows
speeds: List<Speed>
attacks: List<Attack>
space: String
reach: String
specialAttacks: List<String>
specialDefenses: List<SpecialDefense>
tacticalNotes: List<String>
shortAbilities: List<AbilitySummary>
expandedAbilities: List<AbilityDetail>
fullStatBlock: String
```

### Reglas

- `id` debe ser único y estable.
- `summonLevel` indica el nivel mínimo de `Summon Monster`.
- `allowedTemplates` puede estar vacío.
- El JSON se considera correcto.
- La aplicación no valida legalidad completa de Pathfinder.
- No debe contener reglas fijas ya aplicadas salvo que el dato base real lo indique.

### Ejemplo conceptual

```json
{
  "id": "badger",
  "name": "Badger",
  "summonLevel": 1,
  "alignment": "N",
  "size": "SMALL",
  "creatureType": "animal",
  "subtypes": [],
  "allowedTemplates": ["CELESTIAL", "FIERY", "CHTHONIC", "ENTROPIC", "RESOLUTE"]
}
```

---

## ResolvedCreature

Representa la criatura final invocable tras aplicar reglas fijas del personaje.

También puede llamarse `FinalCreature`, pero se recomienda `ResolvedCreature` porque indica que ya se han resuelto las transformaciones necesarias.

Es la ficha que se usa en mesa.

### Campos

```text
id: ResolvedCreatureId
baseTemplateId: CreatureTemplateId
displayName: String
summonLevel: SummonLevel
appliedTemplate: SummonTemplateType?
alignment: Alignment
size: CreatureSize
creatureType: CreatureType
subtypes: List<CreatureSubtype>
initiative: Modifier
senses: List<Sense>
perception: Modifier
abilities: AbilityScores
armorClass: ArmorClass
maxHitPoints: HitPoints
savingThrows: SavingThrows
speeds: List<Speed>
attacks: List<Attack>
space: String
reach: String
specialAttacks: List<String>
specialDefenses: List<SpecialDefense>
shortAbilities: List<AbilitySummary>
expandedAbilities: List<AbilityDetail>
fullStatBlock: String?
appliedRules: List<AppliedRule>
```

### Reglas

- Se genera desde `CreatureTemplate`.
- Debe aplicar `Augment Summoning`.
- Debe aplicar la plantilla seleccionada si procede.
- Debe aplicar `Deep Guardian` si procede.
- Es la fuente de datos para pantalla principal, ficha expandida y tiradas.
- No necesita persistirse si puede reconstruirse de forma determinista, aunque puede cachearse.

### Identidad

La identidad funcional debe derivarse de:

```text
baseTemplateId + appliedTemplate + appliedRules relevantes
```

Ejemplos:

```text
badger:fiery:augment-summoning:deep-guardian
badger:celestial:augment-summoning:deep-guardian
small-earth-elemental:none:augment-summoning:deep-guardian
```

---

## ActiveSummonGroup

Representa un grupo de criaturas activas del mismo tipo final.

Es el bloque que aparece en pantalla principal.

### Campos

```text
id: ActiveSummonGroupId
resolvedCreatureId: ResolvedCreatureId
resolvedCreature: ResolvedCreature
instances: List<ActiveSummonInstance>
createdAt: Instant
updatedAt: Instant
```

### Reglas

- Todas las instancias del grupo comparten la misma `ResolvedCreature`.
- Si se invoca otra criatura con la misma `ResolvedCreature`, se añaden instancias a este grupo.
- Si se invoca la misma criatura base con otra plantilla, se crea otro grupo.
- Si el grupo se queda sin instancias, debe desaparecer de la pantalla.

### Datos mostrados en pantalla

El grupo permite mostrar:

```text
Fiery Badger x5
```

y debajo:

```text
Fiery Badger 1
Fiery Badger 2
Fiery Badger 3
...
```

---

## ActiveSummonInstance

Representa una criatura concreta actualmente presente en combate.

Es la unidad que tiene PG actuales propios.

### Campos

```text
id: ActiveSummonInstanceId
groupId: ActiveSummonGroupId
displayNumber: int
displayName: String
currentHitPoints: HitPoints
maxHitPoints: HitPoints
status: ActiveSummonStatus
createdAt: Instant
updatedAt: Instant
```

### Reglas

- Cada instancia tiene PG actuales independientes.
- `maxHitPoints` normalmente se copia desde `ResolvedCreature.maxHitPoints`.
- Puede tener PG negativos.
- No se elimina automáticamente al llegar a 0 PG o menos.
- El estado visible/API de la instancia se deriva de sus PG actuales y máximos.
- Si `currentHitPoints == maxHitPoints`, su estado es `HEALTHY`.
- Si `0 < currentHitPoints < maxHitPoints`, su estado es `DAMAGED`.
- Si `currentHitPoints <= 0`, su estado es `DOWN`.
- Si se cura por encima de 0 PG, pasa a `DAMAGED` o `HEALTHY` según sus PG actuales.
- Puede eliminarse manualmente.
- Eliminar una instancia implica borrarla del grupo, no marcarla con un estado persistido.
- Si el grupo se queda sin instancias, el grupo desaparece del estado de combate.

### Estados

```text
HEALTHY
DAMAGED
DOWN
```

Reglas de cálculo:

- `HEALTHY`: `currentHitPoints == maxHitPoints`.
- `DAMAGED`: `currentHitPoints > 0 && currentHitPoints < maxHitPoints`.
- `DOWN`: `currentHitPoints <= 0`.

Notas:

- `REMOVED` no es un estado visible, de API ni persistido; eliminar una criatura implica borrar la instancia activa.
- `ACTIVE` no debe usarse como estado de instancia porque es ambiguo frente a `HEALTHY` y `DAMAGED`.
- `DAMAGED` y `DOWN` pueden calcularse dinámicamente a partir de PG actuales y máximos.

---

## CombatState

Representa el estado actual de la pantalla de combate.

### Campos

```text
id: CombatStateId
groups: List<ActiveSummonGroup>
dailyUses: DailyUses
configuration: SummonerConfiguration
lastRollResult: RollDisplay?
recentlyUsedSummons: List<SummonShortcut>
mostUsedSummons: List<SummonShortcut>
updatedAt: Instant
```

### Reglas

- Debe persistirse tras cada cambio relevante.
- Debe recuperarse al recargar la página.
- No gestiona iniciativa.
- No gestiona enemigos.
- No gestiona duración por asaltos.
- No gestiona mapa.

### Acciones que modifican CombatState

- invocar;
- ajustar PG;
- eliminar instancia;
- limpiar invocaciones;
- modificar usos diarios;
- resetear usos diarios.

---

# Configuración del personaje

## SummonerConfiguration

Representa la configuración funcional del personaje invocador.

### Campos

```text
maxSummonMonsterLevel: SummonLevel
dailyUsesMaximum: int
dailyUsesRemaining: int
enabledFixedRules: Set<FixedRuleType>
availableTemplates: Set<SummonTemplateType>
```

### Reglas

- `maxSummonMonsterLevel` determina disponibilidad y cantidad.
- En la situación inicial, el valor esperado es `3`.
- La configuración está acoplada al personaje.
- No está pensada para múltiples usuarios o múltiples personajes.

---

## DailyUses

Representa el contador de usos diarios.

### Campos

```text
maximum: int
remaining: int
```

### Operaciones

```text
consumeOne()
increase(amount: PositiveAmount)
decrease(amount: PositiveAmount)
reset()
updateMaximum(newMaximum: int)
```

### Invariantes

```text
maximum >= 0
0 <= remaining <= maximum
```

`DailyUses` no debe poder construirse ni persistirse con valores fuera de esas invariantes.

### Reglas

- Invocar llama a `consumeOne()`.
- `consumeOne()` resta 1 solo si `remaining > 0`.
- Si `remaining = 0`, `consumeOne()` mantiene `remaining = 0` y no bloquea rígidamente la invocación en el MVP.
- `increase(amount)` suma una cantidad positiva y acota el resultado a `maximum`.
- `decrease(amount)` resta una cantidad positiva y acota el resultado a 0.
- `reset()` establece `remaining = maximum`.
- `updateMaximum(newMaximum)` exige `newMaximum >= 0` y ajusta `remaining = min(remaining, newMaximum)`.
- La corrección manual se hace mediante sumar, restar, resetear o actualizar configuración, nunca mediante valores negativos persistidos.

---

# Reglas fijas

## FixedRuleType

Enumeración de reglas fijas del personaje.

```text
AUGMENT_SUMMONING
SUPERIOR_SUMMONING
VERSATILE_SUMMON_MONSTER
DEEP_GUARDIAN
```

---

## AppliedRule

Representa una regla aplicada a una criatura final.

### Campos

```text
type: FixedRuleType
description: String
```

### Ejemplos

```text
AUGMENT_SUMMONING — +4 Str, +4 Con
DEEP_GUARDIAN — +1 attack, +1 AC
VERSATILE_SUMMON_MONSTER — Fiery template
```

---

## CreatureResolver

Servicio de dominio que genera una `ResolvedCreature` desde una `CreatureTemplate`.

### Entrada

```text
CreatureTemplate
SelectedSummonTemplate?
SummonerConfiguration
```

### Salida

```text
ResolvedCreature
```

### Responsabilidades

- aplicar `Augment Summoning`;
- aplicar plantilla seleccionada;
- aplicar `Deep Guardian`;
- recalcular campos funcionales afectados;
- devolver una criatura final lista para mesa.

### No responsabilidades

- no persiste;
- no consulta UI;
- no decide qué criatura conviene invocar;
- no valida si la criatura es legal en Pathfinder.

---

# Invocación

## SummonRequest

Representa la petición de invocar una criatura.

### Campos

```text
creatureTemplateId: CreatureTemplateId
selectedTemplate: SummonTemplateType?
source: SummonSelectionSource
```

### Reglas

- Si `source = MANUAL_SEARCH` y la criatura permite varias plantillas, debe existir `selectedTemplate`.
- Si `source = RECENT` o `MOST_USED`, la plantilla puede venir implícita en el shortcut.
- El usuario no introduce cantidad manual en el MVP.

---

## SummonSelectionSource

Origen de la selección de invocación.

```text
MANUAL_SEARCH
RECENT
MOST_USED
FAVORITE
```

Notas:

- `FAVORITE` puede quedar preparado para fase futura.
- El diagrama de elección queda fuera del MVP.

---

## SummonShortcut

Representa una combinación reutilizable de criatura + plantilla.

Se usa para últimas usadas, más usadas y futuros favoritos.

### Campos

```text
creatureTemplateId: CreatureTemplateId
selectedTemplate: SummonTemplateType?
displayName: String
usageCount: int
lastUsedAt: Instant?
```

### Reglas

- Al seleccionar desde últimas usadas o más usadas, no se vuelve a preguntar plantilla.
- Reutiliza directamente la combinación guardada.

---

## SummonQuantity

Representa la cantidad de criaturas que aparecen.

### Campos

```text
formula: DiceFormula?
result: int
reason: String
```

### Ejemplos

```text
Nivel máximo: result = 1, formula = null
Nivel máximo - 1: formula = 1d3+1
Nivel máximo - 2: formula = 1d4+2
```

---

## SummonQuantityCalculator

Servicio de dominio que calcula cuántas criaturas aparecen.

### Entrada

```text
creatureSummonLevel: SummonLevel
maxSummonMonsterLevel: SummonLevel
diceRoller: DiceRoller
```

### Salida

```text
SummonQuantity
```

### Reglas

| Relación con nivel máximo | Cantidad |
|---|---:|
| Nivel máximo | 1 |
| Nivel máximo - 1 | `1d3 + 1` |
| Nivel máximo - 2 | `1d4 + 2` |

### Restricciones

- No preguntar cantidad al usuario en el MVP.
- Si la criatura tiene nivel no soportado por la regla, devolver error funcional claro.

---

# Ataques

## Attack

Representa un ataque de una criatura.

### Campos

```text
id: AttackId
name: String
attackBonus: Modifier
quantity: int
attackType: AttackType
damageComponents: List<DamageComponent>
critical: CriticalProfile
notes: List<String>
```

### Reglas

- `quantity` indica cuántas veces se repite el ataque.
- Si `quantity = 2`, se tiran dos ataques separados.
- `damageComponents` debe ser lista para mantener daños separados por tipo.
- No debe mezclarse todo el daño en un único total.

---

## AttackType

Tipo general de ataque.

```text
MELEE
RANGED
TOUCH
SPECIAL
```

El MVP puede tirar todos los ataques definidos, pero no debe automatizar reglas contextuales asociadas a ataques especiales.

---

## DamageComponent

Representa una parte del daño.

### Campos

```text
formula: DiceFormula
damageType: DamageType
multipliesOnCritical: boolean
label: String?
```

### Ejemplo

```text
1d3+3 piercing
1 fire
```

### Reglas

- Los componentes deben mostrarse separados.
- El daño elemental adicional normalmente no se multiplica en crítico.
- Si hay duda, el JSON debe indicar explícitamente `multipliesOnCritical`.

---

## DamageType

Tipo de daño.

Valores iniciales recomendados:

```text
PIERCING
SLASHING
BLUDGEONING
FIRE
COLD
ACID
ELECTRICITY
SONIC
FORCE
UNTYPED
OTHER
```

---

## CriticalProfile

Representa el perfil de crítico de un ataque.

### Campos

```text
threatRangeStart: int
multiplier: int
```

### Ejemplos

```text
20/x2 => threatRangeStart = 20, multiplier = 2
19-20/x2 => threatRangeStart = 19, multiplier = 2
18-20/x3 => threatRangeStart = 18, multiplier = 3
```

### Valor por defecto

Si no se informa crítico:

```text
20/x2
```

---

# Salvaciones

## SavingThrows

Representa las tres tiradas de salvación de una criatura.

### Campos

```text
fortitude: Modifier
reflex: Modifier
will: Modifier
```

### Reglas

- En el MVP se tiran siempre las tres juntas con el botón `Salvaciones`.
- No hay botones separados para Fortaleza, Reflejos y Voluntad.

---

# Tiradas

## DiceFormula

Representa una fórmula de dados.

### Campos

```text
raw: String
diceCount: int
dieSize: int
modifier: int
```

### Ejemplos

```text
1d20+5
1d6+3
1d3+1
1d4+2
```

### Reglas

- Debe poder representar fórmulas simples.
- No hace falta soportar expresiones arbitrarias complejas en el MVP.
- Debe ser testeable con resultados deterministas.

---

## DiceRoll

Representa una tirada concreta de dados.

### Campos

```text
formula: DiceFormula
naturalResults: List<int>
modifier: int
total: int
label: String?
```

### Ejemplo

```text
formula = 1d20+5
naturalResults = [14]
modifier = 5
total = 19
label = Bite
```

---

## DiceRoller

Servicio de dominio para tirar dados.

### Operaciones

```text
roll(formula: DiceFormula): DiceRoll
rollD20(modifier: Modifier, label: String): DiceRoll
```

### Reglas

- Debe permitir implementación aleatoria real.
- Debe permitir implementación determinista para tests.
- La lógica de dominio no debe depender directamente de `Random` sin abstracción.

---

## RollResult

Resultado genérico visible de una tirada.

### Campos

```text
id: RollResultId
label: String
rolls: List<DiceRoll>
total: int?
createdAt: Instant
```

### Uso

Puede usarse para resultados sencillos o como base para resultados más específicos.

Para ataques y TS se recomiendan tipos más explícitos.

---

## RollDisplay

Representa el resultado más reciente visible en pantalla.

### Campos

```text
id: RollDisplayId
title: String
type: RollDisplayType
createdAt: Instant
content: String
structuredResult: Object?
```

### Reglas

- El MVP no requiere historial completo.
- Solo debe conservarse resultado actual o más reciente.
- Puede sustituirse al hacer otra tirada.

---

## RollDisplayType

```text
ATTACK_GROUP
SAVING_THROWS_GROUP
SUMMON_QUANTITY
GENERIC
```

---

# Resultado de ataques

## GroupAttackRollResult

Resultado de pulsar `Atacar`.

### Campos

```text
groupId: ActiveSummonGroupId
creatureName: String
instanceResults: List<CreatureAttackRollResult>
createdAt: Instant
```

### Reglas

- Debe agrupar resultados por criatura individual.
- Debe incluir ataque y daño.
- Debe incluir críticos cuando proceda.
- No decide impactos.
- No aplica daño.

---

## CreatureAttackRollResult

Resultado de ataques de una instancia concreta.

### Campos

```text
instanceId: ActiveSummonInstanceId
instanceDisplayName: String
attackResults: List<SingleAttackRollResult>
```

---

## SingleAttackRollResult

Resultado de un ataque individual.

### Campos

```text
attackId: AttackId
attackName: String
attackIndex: int?
attackRoll: DiceRoll
normalDamage: DamageRollResult
criticalThreat: CriticalThreatResult?
notes: List<String>
```

### Reglas

- `attackIndex` se usa para ataques repetidos como `Claw 1`, `Claw 2`.
- Siempre debe existir `normalDamage`.
- `criticalThreat` solo existe si la tirada natural amenaza crítico.

---

## CriticalThreatResult

Resultado de amenaza de crítico.

### Campos

```text
confirmationRoll: DiceRoll
criticalDamage: DamageRollResult
```

### Reglas

- Se calcula si la tirada natural está dentro del rango de amenaza.
- La aplicación no decide si el crítico confirma contra la CA enemiga.
- El daño normal debe mostrarse siempre aunque exista crítico.

---

## DamageRollResult

Resultado de daño de un ataque.

### Campos

```text
components: List<DamageComponentRollResult>
total: int
label: String?
```

### Reglas

- Puede calcular total internamente.
- La UI debe mostrar componentes separados.
- No debe aplicar daño automáticamente.

---

## DamageComponentRollResult

Resultado de un componente de daño.

### Campos

```text
formula: DiceFormula
roll: DiceRoll
damageType: DamageType
multipliesOnCritical: boolean
total: int
```

### Ejemplo visible

```text
1d3+3 = 5 piercing + 1 fire
```

---

# Resultado de TS

## GroupSavingThrowsRollResult

Resultado de pulsar `Salvaciones`.

### Campos

```text
groupId: ActiveSummonGroupId
creatureName: String
instanceResults: List<CreatureSavingThrowsRollResult>
createdAt: Instant
```

---

## CreatureSavingThrowsRollResult

Resultado de TS de una instancia concreta.

### Campos

```text
instanceId: ActiveSummonInstanceId
instanceDisplayName: String
fortitude: DiceRoll
reflex: DiceRoll
will: DiceRoll
```

### Reglas

- Siempre incluye las tres salvaciones.
- No resuelve consecuencias de éxito o fallo.

---

# Defensas especiales

## SpecialDefense

Representa una defensa que debe mostrarse como aviso en el modal de daño.

### Campos

```text
type: SpecialDefenseType
value: String
notes: String?
```

### Ejemplos

```text
RD 5/good
Resistencia fire 10
Inmune poison
```

---

## SpecialDefenseType

```text
DAMAGE_REDUCTION
RESISTANCE
IMMUNITY
VULNERABILITY
OTHER
```

### Reglas

- Se muestran como nota.
- No se aplican automáticamente al daño.

---

# Valores de características

## AbilityScores

Características de la criatura.

### Campos

```text
strength: int
dexterity: int
constitution: int
intelligence: int
wisdom: int
charisma: int
```

### Reglas

- `Augment Summoning` modifica `strength` y `constitution`.
- El dominio debe recalcular los valores funcionales afectados.

---

## Modifier

Value object para bonificadores.

### Campos

```text
value: int
```

### Ejemplos

```text
+4
-1
0
```

Puede ayudar a evitar confundir puntuaciones de característica con modificadores.

---

## PositiveAmount

Value object para cantidades introducidas por el usuario en operaciones de mesa que requieren un valor positivo.

### Campos

```text
value: int
```

### Reglas

- Debe ser obligatorio.
- Debe ser un número entero.
- Debe ser mayor o igual que 1.
- `0`, números negativos, decimales, texto, `null`, vacío o ausencia de valor son inválidos.
- El signo de la operación no forma parte del valor: el servicio o endpoint determina si el valor se suma o se resta.

### Usos iniciales

```text
damage(amount: PositiveAmount)
heal(amount: PositiveAmount)
increaseDailyUses(amount: PositiveAmount)
decreaseDailyUses(amount: PositiveAmount)
```

---

# CA y PG

## ArmorClass

Representa las clases de armadura relevantes.

### Campos

```text
normal: int
touch: int
flatFooted: int
detail: String?
```

### Reglas

- `Deep Guardian` suma `+1` a `normal`.
- No es necesario recalcular todos los componentes de CA salvo reglas fijas previstas.

---

## HitPointsDefinition

PG base de una criatura del catálogo.

### Campos

```text
maximum: int
formula: String?
hitDice: HitDice?
```

### Uso

Se usa en `CreatureTemplate`.

---

## HitPoints

Value object para puntos de golpe.

### Campos

```text
value: int
```

### Reglas

- Puede ser 0 o negativo para PG actuales.
- PG máximos deben ser positivos.

---

## HitDice

Opcional para recalcular PG con Constitución.

### Campos

```text
diceCount: int
dieSize: int
constitutionModifier: int?
```

### Nota

Si no se implementa cálculo completo de dados de golpe en el MVP, se puede recalcular el incremento funcional de Constitución sobre los PG máximos base.

---

# Movimiento y tipo

## Speed

Representa una velocidad de la criatura.

### Campos

```text
type: SpeedType
valueFeet: int
maneuverability: String?
notes: String?
```

### Ejemplo

```text
burrow 10 ft.
fly 60 ft. good
```

---

## SpeedType

```text
LAND
FLY
SWIM
CLIMB
BURROW
OTHER
```

`BURROW` es relevante para `Deep Guardian`.

---

## CreatureSubtype

Value object o enum flexible para subtipos.

Ejemplos:

```text
earth
elemental
extraplanar
good
evil
fire
water
air
```

El subtipo `earth` es relevante para `Deep Guardian`.

---

## CreatureType

Tipo principal de criatura.

Puede modelarse como enum flexible o string controlado.

Ejemplos:

```text
animal
elemental
outsider
magical beast
vermin
```

---

## CreatureSize

Tamaño.

Valores iniciales:

```text
FINE
DIMINUTIVE
TINY
SMALL
MEDIUM
LARGE
HUGE
GARGANTUAN
COLOSSAL
```

---

## Alignment

Alineamiento.

Valores iniciales:

```text
LG
NG
CG
LN
N
CN
LE
NE
CE
```

La aplicación no valida restricciones legales de alineamiento en el MVP.

---

# Plantillas

## SummonTemplateType

Plantillas soportadas por `Versatile Summon Monster`.

```text
CHTHONIC
FIERY
CELESTIAL
ENTROPIC
RESOLUTE
```

---

## TemplateApplication

Representa la aplicación de una plantilla.

### Campos

```text
type: SummonTemplateType
displayNamePrefix: String
addedSubtypes: List<CreatureSubtype>
alignmentAdjustment: Alignment?
attackAdjustments: List<AttackAdjustment>
damageComponentsAdded: List<DamageComponent>
specialDefensesAdded: List<SpecialDefense>
abilitiesAdded: List<AbilityDetail>
```

### Nota

No hace falta que el MVP tenga un motor complejo de plantillas si basta con reglas explícitas y testeadas.

Pero el dominio debe dejar claro que la plantilla transforma la criatura base para generar la criatura final.

---

# Habilidades y texto

## AbilitySummary

Resumen breve de una habilidad.

### Campos

```text
name: String
summary: String
```

Puede mostrarse en ficha expandida o fases futuras.

---

## AbilityDetail

Descripción completa o suficientemente detallada de una habilidad.

### Campos

```text
name: String
text: String
```

Debe estar disponible en `Expandir ficha`.

---

# Identificadores recomendados

Usar value objects o tipos claros para evitar mezclar identificadores.

```text
CreatureTemplateId
ResolvedCreatureId
ActiveSummonGroupId
ActiveSummonInstanceId
AttackId
RollResultId
CombatStateId
```

En implementación inicial pueden ser strings o UUIDs, pero deben tener significado claro.

---

# Servicios de dominio recomendados

## CreatureCatalogService

Responsable de consultar criaturas base.

Operaciones:

```text
findById(CreatureTemplateId)
findAvailableBySummonLevel(SummonLevel maxLevel)
search(String query)
```

No debe aplicar reglas fijas. Solo devuelve `CreatureTemplate`.

---

## CreatureResolver

Responsable de generar `ResolvedCreature`.

Operaciones:

```text
resolve(CreatureTemplate template, SummonTemplateType selectedTemplate, SummonerConfiguration config)
```

---

## SummoningService

Responsable de ejecutar la invocación funcional.

Operaciones:

```text
summon(SummonRequest request): SummonResult
```

Responsabilidades:

- localizar criatura base;
- resolver criatura final;
- calcular cantidad;
- crear instancias;
- añadir a grupo existente o crear grupo nuevo;
- descontar uso diario;
- actualizar últimas/más usadas;
- persistir estado mediante capa de aplicación o repositorio.

---

## HitPointService

Responsable de aplicar daño y curación.

Operaciones:

```text
damage(instanceId, amount: PositiveAmount)
heal(instanceId, amount: PositiveAmount)
remove(instanceId)
```

Reglas:

- daño individual;
- curación individual;
- daño y curación solo aceptan `PositiveAmount`;
- si la cantidad es inválida, no modificar PG, no cambiar estado y no persistir cambios;
- `/damage` resta el valor positivo;
- `/heal` suma el valor positivo;
- permitir PG negativos;
- marcar caída a 0 o menos;
- no eliminar automáticamente.

---

## AttackRollService

Responsable de `Atacar`.

Operaciones:

```text
rollGroupAttacks(groupId): GroupAttackRollResult
```

Reglas:

- tira todos los ataques de todas las instancias;
- respeta `quantity`;
- tira daño;
- detecta amenaza;
- tira confirmación y daño crítico;
- no decide impacto;
- no aplica daño.

---

## SavingThrowRollService

Responsable de `Salvaciones`.

Operaciones:

```text
rollGroupSavingThrows(groupId): GroupSavingThrowsRollResult
```

Reglas:

- tira Fort, Ref y Vol para cada instancia;
- no resuelve consecuencias.

---

# Repositorios recomendados

## CreatureTemplateRepository

Lee criaturas base desde JSON o almacenamiento equivalente.

```text
findById(id)
findAll()
search(query)
```

## CombatStateRepository

Persiste el estado activo.

```text
load()
save(combatState)
clearActiveSummons()
```

## ConfigurationRepository

Persiste configuración del personaje.

```text
load()
save(configuration)
```

---

# DTOs y API

El dominio no debe depender de DTOs REST.

Los controladores deben convertir entre:

- request REST;
- comandos de aplicación;
- objetos de dominio;
- response REST.

Ejemplos de DTOs posibles:

```text
SummonCreatureRequestDto
DamageCreatureRequestDto
HealCreatureRequestDto
GroupAttackRollResponseDto
GroupSavingThrowsRollResponseDto
CombatStateResponseDto
```

---

# Invariantes del dominio

Estas reglas deben mantenerse siempre:

1. Una instancia activa pertenece a un único grupo.
2. Un grupo representa una única criatura final.
3. Todas las instancias de un grupo comparten ficha final.
4. Cada instancia tiene PG actuales independientes.
5. Una instancia puede tener PG negativos.
6. Una instancia no se elimina automáticamente por llegar a 0 PG.
7. El daño se aplica a una sola instancia.
8. La curación se aplica a una sola instancia.
9. Las criaturas iguales con distinta plantilla son grupos distintos.
10. Las criaturas iguales con misma plantilla se agrupan.
11. `Atacar` no aplica daño.
12. `Salvaciones` no resuelve consecuencias.
13. Los daños se conservan separados por tipo.
14. El daño normal se muestra aunque haya amenaza de crítico.
15. La ficha expandida muestra criatura final, no criatura base.
16. El estado se persiste tras cambios relevantes.
17. La aplicación no gestiona modificadores temporales.
18. La aplicación no usa reglas de PF2e, D&D 5e ni Starfinder.

---

# Estados calculados recomendados

El estado visible/API de una instancia puede calcularse a partir de sus PG actuales y máximos.
No deben añadirse otros estados de instancia en el MVP.

## Estado visual/API de instancia

```text
if currentHitPoints <= 0 => DOWN
else if currentHitPoints == maxHitPoints => HEALTHY
else if currentHitPoints < maxHitPoints => DAMAGED
```

Equivalencias funcionales:

- `HEALTHY`: `currentHitPoints == maxHitPoints`.
- `DAMAGED`: `currentHitPoints > 0 && currentHitPoints < maxHitPoints`.
- `DOWN`: `currentHitPoints <= 0`.

`REMOVED` no se modela como estado: si una criatura se elimina, se borra la instancia activa.

## Grupo vacío

```text
if instances.isEmpty() => remove group from active combat state
```

## Deep Guardian

```text
if speeds contains BURROW OR subtypes contains "earth" => +1 attack, +1 AC
```

---

# Ejemplo de flujo de invocación en dominio

```text
SummonRequest
  creatureTemplateId = badger
  selectedTemplate = FIERY
  source = MANUAL_SEARCH

SummoningService
  carga CreatureTemplate badger
  CreatureResolver genera ResolvedCreature fiery-badger
  SummonQuantityCalculator tira 1d4+2
  crea N ActiveSummonInstance
  busca ActiveSummonGroup de fiery-badger
  si existe, añade instancias
  si no existe, crea grupo
  descuenta 1 uso diario
  actualiza últimas/más usadas
  persiste CombatState
```

---

# Ejemplo de flujo de ataque en dominio

```text
AttackRollService.rollGroupAttacks(groupId)

Para cada ActiveSummonInstance del grupo:
  Para cada Attack de ResolvedCreature.attacks:
    Repetir según Attack.quantity:
      tirar d20 + attackBonus
      tirar daño normal
      si amenaza crítico:
        tirar confirmación
        tirar daño crítico
      crear SingleAttackRollResult

Devolver GroupAttackRollResult
```

---

# Ejemplo de flujo de TS en dominio

```text
SavingThrowRollService.rollGroupSavingThrows(groupId)

Para cada ActiveSummonInstance del grupo:
  tirar d20 + fortitude
  tirar d20 + reflex
  tirar d20 + will

Devolver GroupSavingThrowsRollResult
```

---

# Decisiones de implementación

## Nombres internos

Se recomienda usar nombres internos en inglés para clases y campos:

```text
CreatureTemplate
ResolvedCreature
ActiveSummonGroup
ActiveSummonInstance
Attack
SavingThrows
RollResult
```

La interfaz visible debe estar en español.

## Simplicidad

El dominio debe ser suficientemente expresivo para evitar errores, pero no debe convertirse en un motor universal de Pathfinder.

No modelar lo que el MVP no necesita.

## Testabilidad

Toda la lógica de dominio debe poder probarse sin arrancar Spring ni la interfaz.

Especialmente:

- dados;
- cantidad de invocaciones;
- reglas fijas;
- daño/curación;
- críticos;
- TS;
- agrupación;
- persistencia básica del estado.

---

# Fuera del modelo de dominio MVP

No modelar todavía:

- enemigos;
- iniciativa;
- turnos;
- mapa;
- posición;
- distancia;
- duración en asaltos;
- estados completos;
- buffs/debuffs temporales;
- objetivos de ataques;
- resolución de impacto;
- resolución automática de daño contra enemigos;
- hechizos de criaturas;
- auras;
- diagrama de elección de invocación.

