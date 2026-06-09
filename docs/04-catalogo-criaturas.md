# Catálogo de criaturas

Este documento define cómo debe representarse el catálogo de criaturas invocables.

El catálogo es la fuente de datos funcional que usa la aplicación para:

- listar criaturas disponibles para invocar;
- filtrar por nivel de `Summon Monster`;
- aplicar reglas fijas del personaje;
- generar la ficha final invocable;
- mostrar la ficha resumida en la pantalla principal;
- mostrar la ficha expandida;
- tirar ataques, daños y salvaciones;
- mostrar advertencias útiles al aplicar daño.

La aplicación debe asumir que los datos introducidos en el catálogo son correctos. No debe validar si una criatura es legal, si tiene erratas o si la interpretación de reglas es correcta.

---

# Principio general

Cada criatura del catálogo representa una **criatura base invocable** antes de aplicar las reglas fijas del personaje.

La aplicación generará a partir de esa criatura base una **criatura final invocable**, aplicando cuando corresponda:

- `Augment Summoning`;
- `Versatile Summon Monster`;
- `Deep Guardian`;
- ajustes derivados de la plantilla seleccionada.

Por tanto, el catálogo no debe almacenar únicamente texto plano. Debe contener suficiente información estructurada para que la aplicación pueda calcular ataques, daños, PG, CA y salvaciones finales.

---

# Criatura base y criatura final

## Criatura base

La criatura base es la ficha original almacenada en JSON.

Debe contener:

- datos identificativos;
- nivel de `Summon Monster`;
- estadísticas base;
- defensas;
- velocidades;
- ataques;
- daños;
- salvaciones;
- habilidades;
- ficha completa en texto o estructura expandible.

## Criatura final

La criatura final es la versión generada por la aplicación tras aplicar reglas fijas.

Es la que se muestra en mesa.

Debe ser la que aparece en:

- pantalla principal;
- ficha resumida;
- ficha expandida;
- tiradas de ataque;
- tiradas de daño;
- tiradas de salvación;
- cards individuales de PG.

No es necesario mostrar la criatura base sin transformar durante la partida.

---

# Campos obligatorios de una criatura

Cada criatura invocable debe tener los siguientes campos obligatorios.

```json
{
  "id": "badger",
  "nombre": "Badger",
  "nivelSummon": 1,
  "alineamiento": "N",
  "tamano": "Small",
  "tipo": "animal",
  "subtipos": [],
  "plantillasPermitidas": ["celestial", "fiery", "chthonic", "entropic", "resolute"],
  "iniciativa": 1,
  "sentidos": ["low-light vision", "scent"],
  "percepcion": 5,
  "atributos": {
    "fuerza": 10,
    "destreza": 13,
    "constitucion": 15,
    "inteligencia": 2,
    "sabiduria": 12,
    "carisma": 6
  },
  "ca": {
    "normal": 13,
    "toque": 12,
    "desprevenido": 12,
    "detalle": "+1 Dex, +1 natural, +1 size"
  },
  "pg": {
    "maximos": 6,
    "formula": "1d8+2"
  },
  "salvaciones": {
    "fortaleza": 4,
    "reflejos": 3,
    "voluntad": 1
  },
  "velocidades": [
    {
      "tipo": "land",
      "valor": 30
    },
    {
      "tipo": "burrow",
      "valor": 10
    }
  ],
  "ataques": [],
  "espacio": "5 ft.",
  "alcance": "5 ft.",
  "ataquesEspeciales": ["blood rage"],
  "defensasEspeciales": [],
  "notasTacticas": [],
  "habilidadesResumidas": [],
  "habilidadesCompletas": [],
  "fullStatBlock": ""
}
```

---

# Identificación

## `id`

Identificador único y estable de la criatura base.

Debe cumplir:

- estar en minúsculas;
- no contener espacios;
- usar guiones si es necesario;
- no depender de la plantilla.

Ejemplos:

```text
badger
eagle
small-earth-elemental
wolf
```

La combinación final de criatura + plantilla se derivará aparte.

Ejemplo:

```text
badger + fiery = fiery-badger
badger + celestial = celestial-badger
```

## `nombre`

Nombre visible de la criatura base.

Ejemplos:

```text
Badger
Wolf
Small Earth Elemental
```

La aplicación podrá generar el nombre final añadiendo la plantilla cuando corresponda.

---

# Nivel de Summon Monster

## `nivelSummon`

Nivel mínimo de `Summon Monster` necesario para invocar esa criatura.

Ejemplo:

```json
"nivelSummon": 1
```

La aplicación usará este valor junto a `maxSummonMonsterLevel` para:

- saber si la criatura está disponible;
- calcular si aparece 1 criatura, `1d3 + 1` criaturas o `1d4 + 2` criaturas.

---

# Alineamiento, tipo y subtipo

## `alineamiento`

Alineamiento base de la criatura.

Ejemplos:

```text
N
NG
CG
LG
```

Las criaturas incluidas en el catálogo deben ser opciones realmente invocables para el personaje.

La aplicación no debe validar restricciones de alineamiento.

## `tamano`

Tamaño de la criatura.

Ejemplos:

```text
Small
Medium
Large
Tiny
```

## `tipo`

Tipo principal de criatura.

Ejemplos:

```text
animal
elemental
outsider
magical beast
vermin
```

## `subtipos`

Lista de subtipos.

Ejemplo:

```json
"subtipos": ["earth", "elemental"]
```

Debe ser una lista, aunque esté vacía.

Este campo es importante para detectar reglas como `Deep Guardian`.

---

# Plantillas permitidas

## `plantillasPermitidas`

Lista de plantillas que pueden aplicarse a la criatura mediante `Versatile Summon Monster`.

Ejemplo:

```json
"plantillasPermitidas": ["celestial", "fiery", "chthonic"]
```

Si la criatura no permite plantillas, se usará una lista vacía:

```json
"plantillasPermitidas": []
```

Las plantillas previstas inicialmente son:

- `chthonic`;
- `fiery`;
- `celestial`;
- `entropic`;
- `resolute`.

Si una criatura tiene varias plantillas posibles y se invoca desde búsqueda manual, la aplicación debe preguntar cuál aplicar.

Si se invoca desde últimas usadas o más usadas, puede usarse directamente la combinación previa de criatura + plantilla.

---

# Iniciativa, sentidos y percepción

## `iniciativa`

Bonificador total de iniciativa.

Ejemplo:

```json
"iniciativa": 1
```

## `sentidos`

Lista de sentidos relevantes.

Ejemplo:

```json
"sentidos": ["low-light vision", "scent"]
```

## `percepcion`

Bonificador total de Perception.

Ejemplo:

```json
"percepcion": 5
```

---

# Atributos

## `atributos`

Bloque con las seis características base de la criatura.

Ejemplo:

```json
"atributos": {
  "fuerza": 10,
  "destreza": 13,
  "constitucion": 15,
  "inteligencia": 2,
  "sabiduria": 12,
  "carisma": 6
}
```

Este bloque es obligatorio porque la aplicación debe poder aplicar `Augment Summoning`:

```text
+4 Fuerza
+4 Constitución
```

La aplicación debe recalcular, al menos, los campos funcionales afectados que use en mesa:

- PG máximos;
- ataques cuerpo a cuerpo basados en Fuerza;
- daño cuerpo a cuerpo basado en Fuerza;
- salvación de Fortaleza si depende de Constitución.

---

# Clase de armadura

## `ca`

La CA debe estar estructurada.

Ejemplo:

```json
"ca": {
  "normal": 13,
  "toque": 12,
  "desprevenido": 12,
  "detalle": "+1 Dex, +1 natural, +1 size"
}
```

Campos:

- `normal`: CA normal.
- `toque`: CA de toque.
- `desprevenido`: CA desprevenido.
- `detalle`: texto legible con el desglose.

Si se aplica `Deep Guardian`, la aplicación debe sumar `+1` a la CA normal.

No es necesario que la aplicación recalcule automáticamente todos los componentes de CA salvo las reglas fijas previstas.

---

# Puntos de golpe

## `pg`

Los PG deben incluir valor máximo y fórmula original.

Ejemplo:

```json
"pg": {
  "maximos": 6,
  "formula": "1d8+2"
}
```

Campos:

- `maximos`: PG máximos base.
- `formula`: fórmula de PG en texto.

La aplicación debe generar los PG máximos finales tras aplicar `Augment Summoning`.

Para MVP, si recalcular la fórmula completa no es práctico, puede recalcularse el incremento de Constitución de forma funcional sobre los PG máximos base.

---

# Salvaciones

## `salvaciones`

Bloque con las tres tiradas de salvación base.

Ejemplo:

```json
"salvaciones": {
  "fortaleza": 4,
  "reflejos": 3,
  "voluntad": 1
}
```

La aplicación debe mostrar y tirar:

- Fortaleza;
- Reflejos;
- Voluntad.

El botón de mesa será único:

```text
Tirar TS
```

Ese botón tira las tres salvaciones para todas las criaturas individuales del grupo.

---

# Velocidades

## `velocidades`

Lista de velocidades de la criatura.

Ejemplo:

```json
"velocidades": [
  {
    "tipo": "land",
    "valor": 30
  },
  {
    "tipo": "burrow",
    "valor": 10
  }
]
```

Tipos recomendados:

- `land`;
- `fly`;
- `swim`;
- `climb`;
- `burrow`.

El tipo `burrow` es relevante para aplicar `Deep Guardian`.

La aplicación debe poder mostrar la velocidad en formato legible:

```text
Speed 30 ft., burrow 10 ft.
```

---

# Ataques

Cada ataque debe estar estructurado para permitir tiradas automáticas.

## Campos obligatorios por ataque

Cada ataque debe incluir estos nombres técnicos canónicos:

- `id`;
- `name`;
- `attackBonus`;
- `quantity`;
- `attackType`;
- `damageComponents`;
- `critical`;
- `notes`.

Ejemplo:

```json
{
  "id": "bite",
  "name": "Bite",
  "attackBonus": 3,
  "quantity": 1,
  "attackType": "MELEE",
  "damageComponents": [
    {
      "formula": "1d3+2",
      "damageType": "PIERCING",
      "multipliesOnCritical": true
    }
  ],
  "critical": {
    "threatRangeStart": 20,
    "multiplier": 2
  },
  "notes": []
}
```

## `id`

Identificador estable del ataque.

Ejemplos:

```text
bite
claw
gore
slam
sting
```

## `name`

Nombre visible del ataque.

Ejemplos:

```text
Bite
Claw
Slam
Gore
```

## `attackBonus`

Bonificador total de ataque base.

Ejemplo:

```json
"attackBonus": 3
```

La aplicación debe aplicar sobre este valor las reglas fijas que correspondan, como `Deep Guardian`.

## `quantity`

Número de ataques iguales.

Ejemplo:

```json
"quantity": 2
```

Esto permite representar ataques como:

```text
2 claws +3 (1d2+2)
```

sin duplicar dos veces el mismo ataque en el JSON.

## `attackType`

Tipo general de ataque.

Valores iniciales recomendados:

- `MELEE`;
- `RANGED`;
- `TOUCH`;
- `SPECIAL`.

Para el MVP, la aplicación tirará los ataques estructurados, pero no debe intentar resolver reglas contextuales complejas.

## `damageComponents`

Lista de componentes de daño.

Debe ser una lista para poder mostrar daños separados por tipo.

Ejemplo:

```json
"damageComponents": [
  {
    "formula": "1d3+2",
    "damageType": "PIERCING",
    "multipliesOnCritical": true
  },
  {
    "formula": "1",
    "damageType": "ACID",
    "multipliesOnCritical": false
  }
]
```

La aplicación debe mostrar el resultado separado.

Ejemplo:

```text
Bite: 5 piercing + 1 acid
```

No debe limitarse a mostrar el daño total.

## `critical`

Datos de crítico del ataque.

Ejemplo normal:

```json
"critical": {
  "threatRangeStart": 20,
  "multiplier": 2
}
```

Ejemplo con amenaza 19-20:

```json
"critical": {
  "threatRangeStart": 19,
  "multiplier": 2
}
```

Interpretación:

- `threatRangeStart: 20` significa amenaza solo con 20 natural.
- `threatRangeStart: 19` significa amenaza con 19-20 natural.
- `threatRangeStart: 18` significa amenaza con 18-20 natural.

Cuando un ataque amenace crítico, la aplicación debe mostrar:

- tirada de amenaza;
- tirada de confirmación;
- daño normal;
- daño crítico.

## `notes`

Lista de notas breves del ataque.

Ejemplos:

```json
"notes": ["grab", "trip", "poison"]
```

La aplicación puede mostrar estas notas junto al ataque, pero no debe automatizar reglas contextuales complejas asociadas a ellas.

---

# Daño adicional

El daño adicional debe modelarse como otro componente dentro de `damageComponents`.

Ejemplo:

```json
"damageComponents": [
  {
    "formula": "1d2+2",
    "damageType": "SLASHING",
    "multipliesOnCritical": true
  },
  {
    "formula": "1",
    "damageType": "ACID",
    "multipliesOnCritical": false
  }
]
```

Esto permite representar correctamente ataques con daño elemental, veneno simple, daño de plantilla u otros añadidos.

Si un daño adicional no debe multiplicarse en crítico, puede indicarse explícitamente.

Ejemplo:

```json
{
  "formula": "1",
  "damageType": "ACID",
  "multipliesOnCritical": false
}
```

Si no se informa `multipliesOnCritical`, la aplicación puede asumir `true` solo para el daño físico principal y `false` para daños adicionales elementales.

---

# Habilidades

El catálogo debe separar habilidades resumidas y habilidades completas.

## `habilidadesResumidas`

Lista de recordatorios breves.

Ejemplo:

```json
"habilidadesResumidas": [
  {
    "nombre": "Blood rage",
    "resumen": "Si recibe daño en combate, entra en furia al siguiente asalto."
  }
]
```

Estas habilidades pueden mostrarse en la ficha resumida o en la ficha expandida según el diseño de pantalla.

En el MVP, la pantalla principal prioriza datos mecánicos y no debe saturarse.

## `habilidadesCompletas`

Lista de habilidades con explicación más completa.

Ejemplo:

```json
"habilidadesCompletas": [
  {
    "nombre": "Blood rage",
    "texto": "When a badger takes damage in combat, it flies into a rage on its next turn..."
  }
]
```

Deben mostrarse al usar `Expandir ficha`.

---

# Notas tácticas

## `notasTacticas`

Lista de notas de uso rápido.

Ejemplo:

```json
"notasTacticas": [
  "Buena opción barata para ocupar casillas.",
  "Útil si interesa burrow por terreno o Deep Guardian."
]
```

Aunque este campo existe en el catálogo, la pantalla principal del MVP no debe mostrar notas tácticas en la ficha resumida.

Puede usarse en fases futuras, favoritos, búsqueda, diagrama de elección o ficha expandida.

---

# Defensas especiales

## `defensasEspeciales`

Lista de defensas relevantes para el modal de daño.

Ejemplo:

```json
"defensasEspeciales": [
  {
    "tipo": "resistencia",
    "valor": "fire 5"
  },
  {
    "tipo": "sr",
    "valor": "13"
  },
  {
    "tipo": "inmunidad",
    "valor": "poison"
  },
  {
    "tipo": "rd",
    "valor": "DR 5/good"
  }
]
```

Tipos recomendados:

- `rd`;
- `resistencia`;
- `inmunidad`;
- `sr` o `spell_resistance`;
- `vulnerabilidad`;
- `otra`.

La aplicación debe mostrar estas defensas en el modal de daño.

Compatibilidad con catálogo legado:

- si una entrada antigua guarda `spell resistance` dentro de `otra`, el importador la normaliza a `sr`;
- las defensas generadas por plantillas se calculan en tiempo de resolución y deben seguir siendo visibles en la criatura final;
- las plantillas `celestial`, `entropic` y `resolute` aportan SR en la criatura final, además de sus resistencias y reducción de daño cuando corresponda.

En la pantalla principal, las inmunidades y la SR deben verse a nivel de grupo, mientras que la RD y las resistencias deben verse junto al PG de cada card individual para que el jugador las tenga delante antes de aplicar daño.

La aplicación no debe aplicar automáticamente reducción de daño, resistencias ni inmunidades.

---

# Ficha completa

## `fullStatBlock`

Nombre técnico canónico del texto completo de ficha.

En el catálogo, `fullStatBlock` representa la ficha base almacenada. Puede ser el texto original de la criatura o una referencia completa suficiente para consultar la ficha base.

Ejemplo:

```json
"fullStatBlock": "CR 1/2\nBadger\nN Small animal\nInit +1; Senses low-light vision, scent; Perception +5\n..."
```

La ficha expandida que se muestre en mesa debe ser la ficha final transformada, no necesariamente este texto base literal.

Si la aplicación no puede transformar automáticamente el texto completo, debe al menos mostrar todos los campos estructurados finales y conservar `fullStatBlock` de la criatura base como referencia secundaria, sin presentarlo como ficha principal transformada.

`fichaCompleta` puede usarse como traducción visible en textos de interfaz o documentación explicativa, pero no debe usarse como nombre de campo JSON.

---

# Ejemplo completo mínimo

```json
{
  "id": "badger",
  "nombre": "Badger",
  "nivelSummon": 1,
  "alineamiento": "N",
  "tamano": "Small",
  "tipo": "animal",
  "subtipos": [],
  "plantillasPermitidas": ["celestial", "fiery", "chthonic", "entropic", "resolute"],
  "iniciativa": 1,
  "sentidos": ["low-light vision", "scent"],
  "percepcion": 5,
  "atributos": {
    "fuerza": 10,
    "destreza": 13,
    "constitucion": 15,
    "inteligencia": 2,
    "sabiduria": 12,
    "carisma": 6
  },
  "ca": {
    "normal": 13,
    "toque": 12,
    "desprevenido": 12,
    "detalle": "+1 Dex, +1 natural, +1 size"
  },
  "pg": {
    "maximos": 6,
    "formula": "1d8+2"
  },
  "salvaciones": {
    "fortaleza": 4,
    "reflejos": 3,
    "voluntad": 1
  },
  "velocidades": [
    {
      "tipo": "land",
      "valor": 30
    },
    {
      "tipo": "burrow",
      "valor": 10
    }
  ],
  "ataques": [
    {
      "id": "bite",
      "name": "Bite",
      "attackBonus": 3,
      "quantity": 1,
      "attackType": "MELEE",
      "damageComponents": [
        {
          "formula": "1d3+2",
          "damageType": "PIERCING",
          "multipliesOnCritical": true
        }
      ],
      "critical": {
        "threatRangeStart": 20,
        "multiplier": 2
      },
      "notes": []
    },
    {
      "id": "claw",
      "name": "Claw",
      "attackBonus": 3,
      "quantity": 2,
      "attackType": "MELEE",
      "damageComponents": [
        {
          "formula": "1d2+2",
          "damageType": "SLASHING",
          "multipliesOnCritical": true
        }
      ],
      "critical": {
        "threatRangeStart": 20,
        "multiplier": 2
      },
      "notes": []
    }
  ],
  "espacio": "5 ft.",
  "alcance": "5 ft.",
  "ataquesEspeciales": ["blood rage"],
  "defensasEspeciales": [],
  "notasTacticas": [],
  "habilidadesResumidas": [
    {
      "nombre": "Blood rage",
      "resumen": "Puede entrar en furia tras recibir daño."
    }
  ],
  "habilidadesCompletas": [
    {
      "nombre": "Blood rage",
      "texto": "Texto completo o suficientemente detallado de la habilidad."
    }
  ],
  "fullStatBlock": "CR 1/2\nBadger\nN Small animal\nInit +1; Senses low-light vision, scent; Perception +5\n..."
}
```

---

# Ejemplo de ataque con daño adicional

```json
{
  "id": "bite",
  "name": "Bite",
  "attackBonus": 4,
  "quantity": 1,
  "attackType": "MELEE",
  "damageComponents": [
    {
      "formula": "1d3+2",
      "damageType": "PIERCING",
      "multipliesOnCritical": true
    },
    {
      "formula": "1",
      "damageType": "ACID",
      "multipliesOnCritical": false
    }
  ],
  "critical": {
    "threatRangeStart": 20,
    "multiplier": 2
  },
  "notes": []
}
```

Resultado esperado en tirada:

```text
Bite: ataque 17 | daño 5 piercing + 1 acid
```

Si amenaza crítico:

```text
Bite
Amenaza: 20
Confirmación: 18
Daño normal: 5 piercing + 1 acid
Daño crítico: 10 piercing + 1 acid
```

---

# Reglas de transformación aplicables al catálogo

La aplicación debe cargar la criatura base y generar una criatura final.

## Augment Summoning

Debe aplicar:

```text
+4 Fuerza
+4 Constitución
```

Debe actualizar los valores finales que use la aplicación.

## Versatile Summon Monster

Debe aplicar la plantilla seleccionada si está permitida.

La plantilla puede modificar, añadir o ajustar:

- alineamiento;
- subtipo;
- ataques;
- daño adicional;
- resistencias;
- inmunidades;
- habilidades;
- ficha expandida.

## Deep Guardian

Tras aplicar plantilla, si la criatura final tiene velocidad `burrow` o subtipo `earth`, debe recibir:

```text
+1 al ataque
+1 a la CA
```

---

# Reglas de visualización en mesa

## Pantalla principal

La pantalla principal debe mostrar la ficha resumida final.

Debe incluir datos mecánicos, no textos largos.

No debe mostrar notas tácticas en el MVP.

## Modal de daño

Debe mostrar las defensas especiales relevantes:

- RD;
- resistencias;
- inmunidades;
- vulnerabilidades.

Debe mostrarlas como aviso, no aplicarlas automáticamente.

## Ficha expandida

Debe mostrar la ficha final completa, incluyendo habilidades completas y cualquier texto relevante del catálogo.

---

# Campos que no deben usarse para automatizar en MVP

Aunque el catálogo pueda contener habilidades, notas o textos completos, el MVP no debe automatizar:

- uso de hechizos de criaturas invocadas;
- auras;
- ataques especiales contextuales;
- estados complejos;
- buffs o debuffs temporales;
- modificadores situacionales;
- carga;
- flanqueo;
- inspiración de bardo;
- posicionamiento;
- reglas dependientes del criterio del máster.

Esos datos pueden estar disponibles para consulta, pero no deben convertirse en lógica automática inicial.

---

# Criterios de aceptación

El catálogo de criaturas se considerará suficiente para el MVP cuando permita:

1. Cargar criaturas desde JSON.
2. Identificar cada criatura de forma única.
3. Filtrar criaturas por nivel de `Summon Monster`.
4. Saber qué plantillas puede recibir una criatura.
5. Aplicar `Augment Summoning`.
6. Aplicar una plantilla permitida.
7. Aplicar `Deep Guardian` si procede.
8. Generar una ficha final invocable.
9. Mostrar ficha resumida en pantalla principal.
10. Mostrar ficha expandida.
11. Tirar ataques de todas las criaturas de un grupo.
12. Tirar daños separados por tipo.
13. Detectar amenaza de crítico.
14. Tirar confirmación de crítico.
15. Mostrar daño normal y daño crítico.
16. Tirar Fortaleza, Reflejos y Voluntad.
17. Mostrar defensas especiales en el modal de daño.
18. Evitar automatizar reglas contextuales fuera del MVP.
