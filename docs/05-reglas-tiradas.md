# Reglas funcionales de tiradas

Este documento define cómo debe comportarse la aplicación cuando realiza tiradas automáticas.

La aplicación debe tirar dados para acelerar la mesa, pero no debe sustituir el criterio del jugador ni resolver automáticamente situaciones tácticas o reglas contextuales de Pathfinder 1e.

---

# Principio general

La aplicación puede calcular resultados mecánicos simples:

- tirar dados;
- sumar bonificadores fijos;
- mostrar daño;
- detectar amenaza de crítico;
- tirar confirmación de crítico;
- tirar daño crítico;
- tirar salvaciones;
- tirar número de criaturas invocadas.

La aplicación no debe decidir consecuencias de mesa.

Por tanto, la aplicación no debe:

- decidir si un ataque impacta;
- aplicar daño automáticamente al enemigo;
- aplicar daño automáticamente a criaturas invocadas;
- aplicar modificadores temporales;
- calcular carga, flanqueo, cobertura, inspiración de bardo, bendiciones, penalizadores circunstanciales ni efectos similares;
- resolver estados;
- resolver ataques especiales contextuales;
- decidir si una criatura puede o no actuar;
- interpretar reglas complejas durante la tirada.

El resultado debe ser una ayuda rápida para mesa, no una automatización completa del combate.

---

# Tiradas de ataque

## Acción: Atacar con todas

Cuando el usuario pulsa `Atacar con todas` en un grupo de criaturas, la aplicación debe:

1. Buscar todas las criaturas activas de ese grupo.
2. Obtener la lista de ataques de la ficha final transformada de ese tipo de criatura.
3. Tirar todos los ataques de cada criatura individual.
4. Tirar el daño de cada ataque.
5. Comprobar si alguna tirada amenaza crítico.
6. Si hay amenaza de crítico, tirar confirmación y daño crítico.
7. Mostrar el resultado agrupado por criatura.
8. No aplicar daño automáticamente.

La aplicación debe tirar ataque y daño siempre, aunque luego el usuario determine que el ataque falla.

El daño debe mostrarse como `daño si impacta`.

Los ataques usados en esta tirada deben proceder de la criatura final ya resuelta. `Augment Summoning` debe haber ajustado previamente `attackBonus` y las fórmulas de `damageComponents` usando metadatos explícitos (`attackAbility`, `damageAbility` y `damageAbilityMultiplier`), no interpretando texto libre.

---

# Formato de resultado de ataque

El resultado debe ser legible rápidamente en mesa.

Debe agruparse por criatura individual.

Ejemplo:

```text
Lobo celestial 1
Mordisco: d20 14 + 5 = 19
Daño si impacta: 1d6+3 = 7 piercing

Lobo celestial 2
Mordisco: d20 8 + 5 = 13
Daño si impacta: 1d6+3 = 5 piercing
```

Si una criatura tiene varios ataques, deben mostrarse todos bajo esa criatura.

Ejemplo:

```text
Fiery Badger 1
Bite: d20 12 + 6 = 18
Daño si impacta: 1d3+4 = 6 piercing + 1 fire

Claw 1: d20 18 + 6 = 24
Daño si impacta: 1d2+4 = 5 slashing + 1 fire

Claw 2: d20 7 + 6 = 13
Daño si impacta: 1d2+4 = 5 slashing + 1 fire
```

---

# Ataques con cantidad

Un ataque puede tener una cantidad mayor que 1.

Ejemplo funcional:

```json
{
  "id": "claw",
  "name": "Claw",
  "attackBonus": 6,
  "attackAbility": "STRENGTH",
  "quantity": 2,
  "attackType": "MELEE",
  "damageComponents": [
    {
      "formula": "1d2+4",
      "damageType": "SLASHING",
      "multipliesOnCritical": true,
      "damageAbility": "STRENGTH",
      "damageAbilityMultiplier": 1.0
    },
    {
      "formula": "1",
      "damageType": "FIRE",
      "multipliesOnCritical": false,
      "damageAbility": "NONE",
      "damageAbilityMultiplier": 0
    }
  ],
  "critical": {
    "threatRangeStart": 20,
    "multiplier": 2
  },
  "notes": []
}
```

En ese caso, la aplicación debe tirar ese ataque tantas veces como indique `quantity`.

El resultado debe diferenciar cada repetición.

Ejemplo:

```text
Claw 1: d20 18 + 6 = 24
Daño si impacta: 1d2+4 = 5 slashing + 1 fire

Claw 2: d20 7 + 6 = 13
Daño si impacta: 1d2+4 = 5 slashing + 1 fire
```

---

# Daño

La aplicación debe tirar el daño junto al ataque.

Debe mostrarlo siempre como daño condicionado:

```text
Daño si impacta
```

La aplicación no debe aplicar ese daño a ningún objetivo.

El usuario decidirá en mesa:

- si el ataque impacta;
- si hay reducción de daño;
- si hay resistencia;
- si hay inmunidad;
- si el daño se aplica completo, parcial o no se aplica.

---

# Tipos de daño

Cuando un ataque tenga varios componentes de daño, la aplicación debe mostrarlos separados.

Ejemplo:

```text
Daño si impacta: 1d3+4 = 6 piercing + 1 fire
```

No debe mostrar únicamente:

```text
Daño total: 6
```

La separación por tipo es importante para que el usuario pueda aplicar manualmente resistencias, inmunidades o reducción de daño.

---

# Daño adicional

El daño adicional debe representarse como componentes separados.

Ejemplo:

```json
"damageComponents": [
  {
    "formula": "1d3+4",
    "damageType": "PIERCING",
    "multipliesOnCritical": true,
      "damageAbility": "STRENGTH",
      "damageAbilityMultiplier": 1.0
  },
  {
    "formula": "1",
    "damageType": "ACID",
    "multipliesOnCritical": false,
      "damageAbility": "NONE",
      "damageAbilityMultiplier": 0
  }
]
```

Resultado esperado:

```text
Daño si impacta: 1d3+4 = 6 piercing + 1 acid
```

La aplicación no debe decidir si un componente de daño adicional se multiplica o no en crítico salvo que ese dato esté modelado explícitamente en el ataque.

Además, cada componente debe indicar `damageAbility` y `damageAbilityMultiplier` para que las reglas fijas puedan ajustar el daño de forma determinista. El daño elemental adicional de plantillas normalmente usa `damageAbility: NONE`, `damageAbilityMultiplier: 0` y `multipliesOnCritical: false`.

---

# Críticos

La aplicación debe gestionar críticos de forma automática y mínima.

Debe detectar amenaza de crítico a partir de los datos del ataque.

Cada ataque debe poder definir:

- rango de amenaza;
- multiplicador de crítico.

Si no se define nada, se asume:

```text
crítico: 20/x2
```

## Cuando no hay amenaza

Si la tirada natural del d20 no está dentro del rango de amenaza, se muestra el ataque normal.

Ejemplo:

```text
Mordisco: d20 14 + 5 = 19
Daño si impacta: 1d6+3 = 7 piercing
```

## Cuando hay amenaza

Si la tirada natural del d20 amenaza crítico, la aplicación debe tirar confirmación automáticamente.

Debe mostrar:

- tirada de amenaza;
- total de amenaza;
- tirada de confirmación;
- total de confirmación;
- daño normal;
- daño crítico.

Ejemplo:

```text
Mordisco
Amenaza: d20 20 + 5 = 25
Confirmación: d20 13 + 5 = 18
Daño normal: 1d6+3 = 7 piercing
Daño crítico: 2d6+6 = 12 piercing
```

La aplicación no debe decidir si el crítico se confirma contra la CA del enemigo.

El usuario lo determinará manualmente.

## Daño normal siempre visible

Aunque haya amenaza de crítico, el daño normal debe mostrarse siempre.

Esto permite usar el resultado normal si:

- el crítico no se confirma;
- el enemigo es inmune a críticos;
- el usuario decide corregir algo manualmente;
- hay una regla de mesa o situación especial.

## Componentes de daño en crítico

El catálogo de criaturas debe indicar qué componentes de daño se multiplican en crítico y cuáles no, si hace falta distinguirlo.

Ejemplo conceptual:

```json
"damageComponents": [
  {
    "formula": "1d6+3",
    "damageType": "PIERCING",
    "multipliesOnCritical": true,
      "damageAbility": "STRENGTH",
      "damageAbilityMultiplier": 1.0
  },
  {
    "formula": "1",
    "damageType": "FIRE",
    "multipliesOnCritical": false,
      "damageAbility": "NONE",
      "damageAbilityMultiplier": 0
  }
]
```

`multipliesOnCritical` debe estar indicado explícitamente para cada componente.

El cálculo de daño crítico debe respetar `multipliesOnCritical`:

- si `multipliesOnCritical = true`, el componente se multiplica según el multiplicador de crítico;
- si `multipliesOnCritical = false`, el componente se incluye una vez, sin multiplicarse;
- ningún componente de daño desaparece del daño crítico salvo que una regla explícita lo indique.

Ejemplo:

```text
Daño normal: 1d3+4 = 6 piercing + 1 fire
Daño crítico: 2d3+8 = 12 piercing + 1 fire
```

El daño crítico no debe omitir el componente elemental ni duplicarlo.

Los campos `damageAbility` y `damageAbilityMultiplier` sirven para resolver la criatura final antes de tirar; no autorizan a inferir reglas desde `fullStatBlock` o textos de ficha.

---

# Tiradas de salvación

## Acción: Tirar TS

Cuando el usuario pulsa `Tirar TS` en un grupo de criaturas, la aplicación debe:

1. Buscar todas las criaturas activas de ese grupo.
2. Obtener las tres salvaciones de la ficha final transformada.
3. Tirar Fortaleza para cada criatura.
4. Tirar Reflejos para cada criatura.
5. Tirar Voluntad para cada criatura.
6. Mostrar el resultado agrupado por criatura.

No habrá botones separados para Fortaleza, Reflejos y Voluntad en el MVP.

El botón `Tirar TS` tira siempre las tres salvaciones.

---

# Formato de resultado de TS

El resultado debe ser compacto y legible.

Ejemplo:

```text
Lobo celestial 1
Fortaleza: d20 12 + 4 = 16
Reflejos: d20 8 + 5 = 13
Voluntad: d20 3 + 1 = 4

Lobo celestial 2
Fortaleza: d20 3 + 4 = 7
Reflejos: d20 17 + 5 = 22
Voluntad: d20 10 + 1 = 11
```

También puede mostrarse en formato compacto si mejora la lectura en tablet:

```text
Lobo celestial 1: Fort 16 / Ref 13 / Will 4
Lobo celestial 2: Fort 7 / Ref 22 / Will 11
```

El formato compacto puede ocultar o expandir el detalle del d20 si la interfaz lo permite.

---

# Resultado visible de tirada

La aplicación conserva como máximo el resultado actual o más reciente de tirada.

Cada nueva tirada puede sustituir el resultado anterior.

El usuario debe poder limpiar manualmente ese resultado visible sin limpiar invocaciones, sin modificar PG, sin modificar usos diarios y sin crear historial de tiradas.


---

# Tirada de número de criaturas invocadas

Al invocar, la aplicación debe calcular automáticamente cuántas criaturas aparecen.

La tirada depende del nivel de la criatura respecto al nivel máximo actual de `Summon Monster`.

Regla funcional:

| Relación con nivel máximo | Número de criaturas |
|---|---:|
| Nivel máximo | 1 |
| Nivel máximo - 1 | `1d3 + 1` |
| Nivel máximo - 2 | `1d4 + 2` |

Ejemplo con `maxSummonMonsterLevel = 3`:

| Nivel de criatura | Tirada |
|---|---|
| Summon Monster III | 1 criatura |
| Summon Monster II | `1d3 + 1` criaturas |
| Summon Monster I | `1d4 + 2` criaturas |

El resultado de esta tirada debe usarse para crear las cards individuales correspondientes.

Ejemplo:

```text
Invocación: Fiery Badger
Nivel: Summon Monster I
Cantidad: 1d4 + 2 = 5

Se crean 5 criaturas individuales.
```

---

# Tiradas y persistencia

Las tiradas no necesitan guardarse como historial completo en el MVP.

La aplicación debe mostrar el resultado actual o más reciente.

El estado persistido debe incluir:

- criaturas activas;
- PG actuales;
- usos diarios restantes;
- configuración relevante.

No es obligatorio persistir:

- resultados antiguos de ataque;
- resultados antiguos de daño;
- resultados antiguos de TS;
- historial completo de tiradas.

Si el usuario necesita otra tirada, puede volver a pulsar el botón correspondiente.

---

# Tiradas y criaturas caídas

Una criatura a 0 PG o menos se marca visualmente como caída (`DOWN`).

Para el MVP, la aplicación puede seguir incluyéndola en las tiradas globales mientras siga presente en el grupo.

Motivo:

- algunas criaturas pueden seguir actuando o existiendo por debajo de 0 PG;
- la aplicación no debe decidir estados complejos;
- el usuario puede eliminar manualmente las criaturas que ya no deban participar.

Si en pruebas de mesa resulta molesto, se podrá añadir más adelante una opción para excluir criaturas caídas de tiradas globales.

---

# No automatizar inicialmente

La aplicación no debe automatizar inicialmente:

- impacto contra CA enemiga;
- aplicación de daño a enemigos;
- aplicación de daño a criaturas invocadas desde tiradas enemigas;
- cálculo de reducción de daño;
- cálculo de resistencias;
- cálculo de inmunidades;
- estados;
- concentración;
- ataques de oportunidad;
- flanqueo;
- carga;
- cobertura;
- ocultación;
- inspiración de bardo;
- bendiciones;
- buffs;
- debuffs;
- auras;
- hechizos de criaturas invocadas;
- decisiones tácticas.

---

# Criterios de aceptación

Este documento se considerará implementado cuando la aplicación pueda:

1. Tirar todos los ataques de todas las criaturas de un grupo.
2. Mostrar los resultados agrupados por criatura.
3. Tirar daño junto al ataque.
4. Mostrar el daño como `daño si impacta`.
5. Mostrar daños separados por tipo.
6. Detectar amenaza de crítico.
7. Tirar confirmación de crítico.
8. Mostrar daño normal y daño crítico.
9. Tirar Fortaleza, Reflejos y Voluntad de todas las criaturas de un grupo con un único botón.
10. Tirar el número de criaturas invocadas según el nivel de `Summon Monster`.
11. No decidir si los ataques impactan.
12. No aplicar daño automáticamente.
13. No resolver estados.
14. No aplicar modificadores temporales ni situacionales.
