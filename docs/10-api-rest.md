# API REST

Este documento define la API REST funcional del MVP.

La API debe servir al frontend web local que se usará desde tablet Android durante combate. Su objetivo es exponer operaciones rápidas y claras para:

- consultar el catálogo de criaturas;
- consultar el estado actual de combate;
- invocar criaturas;
- gestionar PG individuales;
- eliminar criaturas;
- limpiar invocaciones;
- tirar ataques de un grupo;
- tirar TS de un grupo;
- gestionar usos diarios;
- consultar ficha expandida.

Este documento es una especificación funcional en Markdown. A partir de él se podrá generar o escribir un contrato `openapi.yaml`.

---

# Principios de diseño de la API

## 1. API orientada a mesa

La API debe favorecer operaciones directas de la pantalla principal.

El frontend no debe tener que reconstruir demasiada lógica de dominio.

Por ejemplo:

- al invocar, la API debe devolver el estado actualizado;
- al dañar, la API debe devolver la instancia actualizada y/o el estado actualizado;
- al tirar ataques, la API debe devolver un resultado ya agrupado por criatura;
- al tirar TS, la API debe devolver las tres salvaciones de todas las criaturas del grupo.

## 2. La API no resuelve Pathfinder completo

La API puede:

- tirar dados;
- calcular número de criaturas invocadas;
- aplicar reglas fijas del personaje;
- generar criatura final;
- actualizar PG;
- detectar criatura caída;
- tirar confirmación de crítico;
- devolver daño normal y crítico;
- devolver daños separados por tipo.

La API no debe:

- decidir si un ataque impacta;
- decidir si un crítico confirma contra la CA enemiga;
- aplicar daño automáticamente a enemigos;
- aplicar RD, resistencias o inmunidades automáticamente;
- resolver estados;
- gestionar iniciativa;
- gestionar enemigos;
- gestionar mapa;
- gestionar duración por asaltos;
- aplicar modificadores temporales.

## 3. Estado persistido tras cada cambio

Toda operación que modifique el estado debe persistirlo.

Deben persistirse, como mínimo:

- invocaciones activas;
- grupos activos;
- instancias individuales;
- PG actuales;
- usos diarios;
- configuración;
- últimas usadas;
- más usadas.

## 4. DTOs explícitos

Los DTOs REST deben ser explícitos y no deben exponer directamente entidades JPA ni clases internas del dominio.

El dominio no debe depender de DTOs REST.

## 5. Interfaz visible en español

Los textos visibles de acciones, labels y resultados pueden venir en español cuando sean parte de la respuesta de pantalla.

Los nombres técnicos de campos pueden estar en inglés o camelCase, pero deben ser consistentes.

---

# Convenciones REST

## Base path

Todos los endpoints del MVP deben colgar de:

```text
/api
```

## Formato

Todas las peticiones y respuestas usan JSON:

```http
Content-Type: application/json
Accept: application/json
```

## Identificadores

Los identificadores se tratan como strings.

Ejemplos:

```text
badger
fiery-badger
group-1
instance-3
```

En implementación pueden ser UUIDs, pero la API no debe obligar al frontend a conocer detalles internos.

## Fechas

Las fechas se devuelven en ISO-8601.

Ejemplo:

```text
2026-06-06T18:45:00Z
```

## Códigos HTTP

Uso recomendado:

| Código | Uso |
|---:|---|
| `200 OK` | Consulta o acción completada con respuesta |
| `201 Created` | Recurso creado, si se usa creación explícita |
| `204 No Content` | Acción completada sin cuerpo, si procede |
| `400 Bad Request` | Petición inválida o error funcional de validación |
| `404 Not Found` | Recurso no encontrado |
| `409 Conflict` | Conflicto de estado |
| `500 Internal Server Error` | Error no esperado |

Para simplificar el MVP, se recomienda que las acciones de mesa devuelvan `200 OK` con el estado actualizado.

---

# Modelo común de error

Todas las respuestas de error deberían seguir una estructura común.

```json
{
  "code": "CREATURE_NOT_FOUND",
  "message": "No existe la criatura base solicitada.",
  "details": {
    "creatureTemplateId": "badger"
  }
}
```

## Campos

| Campo | Tipo | Descripción |
|---|---|---|
| `code` | string | Código técnico estable |
| `message` | string | Mensaje legible |
| `details` | object | Datos opcionales del error |

## Códigos de error recomendados

```text
CREATURE_NOT_FOUND
GROUP_NOT_FOUND
INSTANCE_NOT_FOUND
TEMPLATE_NOT_ALLOWED
TEMPLATE_REQUIRED
INVALID_DAMAGE_AMOUNT
INVALID_HEAL_AMOUNT
UNSUPPORTED_SUMMON_LEVEL
COMBAT_STATE_NOT_FOUND
INVALID_DAILY_USES
VALIDATION_ERROR
```

---

# Resumen de endpoints

## Catálogo

| Método | Endpoint | Uso |
|---|---|---|
| `GET` | `/api/catalog/creatures` | Listar o buscar criaturas base |
| `GET` | `/api/catalog/creatures/{creatureTemplateId}` | Ver detalle de criatura base |
| `GET` | `/api/catalog/creatures/{creatureTemplateId}/resolved-preview` | Previsualizar criatura final |

## Estado de combate

| Método | Endpoint | Uso |
|---|---|---|
| `GET` | `/api/combat-state` | Obtener estado actual de pantalla |
| `POST` | `/api/combat-state/summons` | Invocar criatura |
| `DELETE` | `/api/combat-state/summons` | Limpiar todas las invocaciones |
| `DELETE` | `/api/combat-state/instances/{instanceId}` | Eliminar criatura individual |

## PG

| Método | Endpoint | Uso |
|---|---|---|
| `POST` | `/api/combat-state/instances/{instanceId}/damage` | Aplicar daño individual |
| `POST` | `/api/combat-state/instances/{instanceId}/heal` | Aplicar curación individual |

## Tiradas

| Método | Endpoint | Uso |
|---|---|---|
| `POST` | `/api/combat-state/groups/{groupId}/roll-attacks` | Atacar |
| `POST` | `/api/combat-state/groups/{groupId}/roll-saving-throws` | Salvaciones |

## Usos diarios y configuración

| Método | Endpoint | Uso |
|---|---|---|
| `GET` | `/api/configuration` | Obtener configuración |
| `PATCH` | `/api/configuration` | Actualizar configuración básica |
| `POST` | `/api/daily-uses/increase` | Sumar usos diarios |
| `POST` | `/api/daily-uses/decrease` | Restar usos diarios |
| `POST` | `/api/daily-uses/reset` | Resetear usos diarios |

---

# 1. Catálogo

## GET `/api/catalog/creatures`

Lista criaturas base disponibles en el catálogo.

Debe permitir búsqueda y filtrado básico.

### Query params

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---:|---|
| `query` | string | No | Texto de búsqueda |
| `summonLevel` | integer | No | Filtra por nivel exacto de Summon Monster |
| `maxSummonLevel` | integer | No | Devuelve criaturas de nivel igual o inferior |
| `template` | string | No | Filtra criaturas que no sean `outsider` |
| `limit` | integer | No | Límite de resultados |
| `offset` | integer | No | Desplazamiento |

### Respuesta `200 OK`

```json
{
  "items": [
    {
      "id": "badger",
      "name": "Badger",
      "summonLevel": 1,
      "alignment": "N",
      "size": "SMALL",
      "creatureType": "animal",
      "subtypes": [],
      "allowedTemplates": ["CELESTIAL", "FIERY", "CHTHONIC"],
      "summary": {
        "armorClass": {
          "normal": 13,
          "touch": 12,
          "flatFooted": 12,
          "detail": "+1 Dex, +1 natural, +1 size"
        },
        "maxHitPoints": 6,
        "savingThrows": {
          "fortitude": 4,
          "reflex": 3,
          "will": 1
        },
        "speedsText": "Speed 30 ft., burrow 10 ft.",
        "attacksText": "Melee bite +3 (1d3+2), 2 claws +3 (1d2+2)"
      }
    }
  ],
  "total": 1
}
```

### Reglas

- Devuelve criaturas base, no criaturas finales.
- No aplica reglas fijas del personaje.
- No valida legalidad de invocación más allá de filtros simples.
- Para plantillas, las criaturas que no sean `outsider` se consideran válidas; `outsider` se excluye.
- El frontend puede usarlo para el modal de búsqueda manual.

---

## GET `/api/catalog/creatures/{creatureTemplateId}`

Obtiene detalle completo de una criatura base del catálogo.

### Path params

| Parámetro | Tipo | Descripción |
|---|---|---|
| `creatureTemplateId` | string | ID de criatura base |

### Respuesta `200 OK`

```json
{
  "id": "badger",
  "name": "Badger",
  "summonLevel": 1,
  "challengeRating": "1/2",
  "alignment": "N",
  "size": "SMALL",
  "creatureType": "animal",
  "subtypes": [],
  "allowedTemplates": ["CELESTIAL", "FIERY", "CHTHONIC", "ENTROPIC", "RESOLUTE"],
  "initiative": 1,
  "senses": ["low-light vision", "scent"],
  "perception": 5,
  "abilities": {
    "strength": 10,
    "dexterity": 13,
    "constitution": 15,
    "intelligence": 2,
    "wisdom": 12,
    "charisma": 6
  },
  "armorClass": {
    "normal": 13,
    "touch": 12,
    "flatFooted": 12,
    "detail": "+1 Dex, +1 natural, +1 size"
  },
  "hitPoints": {
    "maximum": 6,
    "formula": "1d8+2"
  },
  "savingThrows": {
    "fortitude": 4,
    "reflex": 3,
    "will": 1
  },
  "speeds": [
    {
      "type": "LAND",
      "valueFeet": 30
    },
    {
      "type": "BURROW",
      "valueFeet": 10
    }
  ],
  "attacks": [
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
  ],
  "space": "5 ft.",
  "reach": "5 ft.",
  "specialAttacks": ["blood rage"],
  "specialDefenses": [],
  "tacticalNotes": [],
  "shortAbilities": [],
  "expandedAbilities": [],
  "fullStatBlock": "CR 1/2\nBadger\nN Small animal..."
}
```

`ExpandedStatBlockResponse.fullStatBlock` sustituye a cualquier alias previo como `statBlockText`. En el MVP no se mantiene retrocompatibilidad con nombres alternativos.

### Errores

| Código | Cuándo |
|---|---|
| `404` | No existe `creatureTemplateId` |

---

## GET `/api/catalog/creatures/{creatureTemplateId}/resolved-preview`

Previsualiza cómo quedaría una criatura final aplicando reglas fijas y plantilla.

Este endpoint es útil para el modal de invocación, pero no crea criaturas activas ni consume usos diarios.

### Query params

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---:|---|
| `template` | string | No | Plantilla a aplicar |

### Ejemplo

```http
GET /api/catalog/creatures/badger/resolved-preview?template=FIERY
```

### Respuesta `200 OK`

```json
{
  "id": "badger:FIERY:AUGMENT_SUMMONING:DEEP_GUARDIAN",
  "baseTemplateId": "badger",
  "displayName": "Fiery Badger",
  "summonLevel": 1,
  "challengeRating": "1/2",
  "appliedTemplate": "FIERY",
  "alignment": "N",
  "size": "SMALL",
  "creatureType": "animal",
  "subtypes": [],
  "initiative": 1,
  "armorClass": {
    "normal": 14,
    "touch": 12,
    "flatFooted": 13,
    "detail": "+1 Dex, +1 natural, +1 size, +1 Deep Guardian"
  },
  "maxHitPoints": 15,
  "savingThrows": {
    "fortitude": 8,
    "reflex": 3,
    "will": 1
  },
  "speedsText": "Speed 30 ft., burrow 10 ft.",
  "attacksText": "Melee bite +4 (1d3+3 + 1 fire), 2 claws +4 (1d2+3 + 1 fire)",
  "specialDefenses": [],
  "appliedRules": [
    {
      "type": "AUGMENT_SUMMONING",
      "description": "+4 Str, +4 Con"
    },
    {
      "type": "VERSATILE_SUMMON_MONSTER",
      "description": "Fiery template"
    },
    {
      "type": "DEEP_GUARDIAN",
      "description": "+1 attack, +1 AC"
    }
  ]
}
```

### Reglas

- No persiste estado.
- No crea instancias.
- No descuenta usos diarios.
- Devuelve criatura final transformada para consulta.

---

# 2. Estado de combate

## GET `/api/combat-state`

Devuelve el estado actual completo necesario para pintar la pantalla principal.

### Respuesta `200 OK`

```json
{
  "groups": [
    {
      "id": "group-1",
      "resolvedCreature": {
        "id": "badger:FIERY:AUGMENT_SUMMONING:DEEP_GUARDIAN",
        "baseTemplateId": "badger",
        "displayName": "Fiery Badger",
        "summonLevel": 1,
        "appliedTemplate": "FIERY",
        "alignment": "N",
        "size": "SMALL",
        "creatureType": "animal",
        "subtypes": [],
        "initiative": 1,
        "senses": ["low-light vision", "scent"],
        "perception": 5,
        "armorClass": {
          "normal": 14,
          "touch": 12,
          "flatFooted": 13,
          "detail": "+1 Dex, +1 natural, +1 size, +1 Deep Guardian"
        },
        "maxHitPoints": 15,
        "savingThrows": {
          "fortitude": 8,
          "reflex": 3,
          "will": 1
        },
        "speedsText": "Speed 30 ft., burrow 10 ft.",
        "attacksText": "Melee bite +4 (1d3+3 + 1 fire), 2 claws +4 (1d2+3 + 1 fire)",
        "space": "5 ft.",
        "reach": "5 ft.",
        "specialAttacks": ["blood rage"],
        "specialDefenses": [],
        "shortAbilities": [],
        "expandedAbilities": [],
        "fullStatBlock": "Fiery Badger\nN Small animal...",
        "appliedRules": [
          {
            "type": "AUGMENT_SUMMONING",
            "description": "+4 Str, +4 Con"
          }
        ]
      },
      "instances": [
        {
          "id": "instance-1",
          "displayNumber": 1,
          "displayName": "Fiery Badger 1",
          "currentHitPoints": 15,
          "maxHitPoints": 15,
          "status": "HEALTHY"
        },
        {
          "id": "instance-2",
          "displayNumber": 2,
          "displayName": "Fiery Badger 2",
          "currentHitPoints": -2,
          "maxHitPoints": 15,
          "status": "DOWN"
        }
      ]
    }
  ],
  "dailyUses": {
    "maximum": 6,
    "remaining": 4
  },
  "configuration": {
    "maxSummonMonsterLevel": 3,
    "availableTemplates": ["CHTHONIC", "FIERY", "CELESTIAL", "ENTROPIC", "RESOLUTE"]
  },
  "lastRollResult": null,
  "recentlyUsedSummons": [
    {
      "creatureTemplateId": "badger",
      "selectedTemplate": "FIERY",
      "displayName": "Fiery Badger",
      "usageCount": 3,
      "lastUsedAt": "2026-06-06T18:45:00Z"
    }
  ],
  "mostUsedSummons": []
}
```

### Reglas

- Es el endpoint principal de carga de pantalla.
- Debe devolver suficiente información para pintar la pantalla sin llamadas extra.
- Las instancias tienen PG independientes.
- El estado visual puede venir calculado como `HEALTHY`, `DAMAGED` o `DOWN`.

---

# 3. Invocar criaturas

## POST `/api/combat-state/summons`

Invoca una criatura y actualiza el estado de combate.

### Request body

```json
{
  "creatureTemplateId": "badger",
  "selectedTemplate": "FIERY",
  "source": "MANUAL_SEARCH"
}
```

### Campos

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---:|---|
| `creatureTemplateId` | string | Sí | ID de criatura base |
| `selectedTemplate` | string/null | No | Plantilla seleccionada |
| `source` | string | Sí | Origen de selección |

### `source`

Valores permitidos:

```text
MANUAL_SEARCH
RECENT
MOST_USED
FAVORITE
```

`FAVORITE` puede quedar reservado para fase posterior.

### Reglas

- Si `source = MANUAL_SEARCH` y la criatura tiene varias plantillas posibles, `selectedTemplate` debe venir informado.
- Si `source = RECENT` o `MOST_USED`, se puede reutilizar la combinación criatura + plantilla.
- La API calcula automáticamente la cantidad.
- La API aplica reglas fijas del personaje.
- La API crea instancias individuales.
- La API agrupa con un grupo existente si coincide la criatura final.
- La API descuenta automáticamente 1 uso diario.
- La API persiste el estado.

### Respuesta `200 OK`

```json
{
  "summonResult": {
    "creatureTemplateId": "badger",
    "resolvedCreatureId": "badger:FIERY:AUGMENT_SUMMONING:DEEP_GUARDIAN",
    "displayName": "Fiery Badger",
    "selectedTemplate": "FIERY",
    "quantity": {
      "formula": "1d4+2",
      "roll": {
        "formula": "1d4+2",
        "naturalResults": [3],
        "modifier": 2,
        "total": 5,
        "label": "Cantidad invocada"
      },
      "result": 5,
      "reason": "Criatura de nivel máximo - 2 con Superior Summoning"
    },
    "createdInstanceIds": [
      "instance-1",
      "instance-2",
      "instance-3",
      "instance-4",
      "instance-5"
    ],
    "groupId": "group-1",
    "dailyUsesRemaining": 3
  },
  "combatState": {
    "groups": [],
    "dailyUses": {
      "maximum": 6,
      "remaining": 3
    },
    "configuration": {
      "maxSummonMonsterLevel": 3
    },
    "lastRollResult": {
      "id": "roll-1",
      "type": "SUMMON_QUANTITY",
      "title": "Cantidad invocada: Fiery Badger",
      "createdAt": "2026-06-06T18:45:00Z",
      "content": "1d4+2 = 5"
    }
  }
}
```

### Nota sobre `combatState`

En los ejemplos, `combatState.groups` puede abreviarse con `[]` para no repetir estructuras largas.

En la API real, debe devolver el estado actualizado completo o una estructura equivalente suficiente para refrescar pantalla.

### Errores

| Código HTTP | Código funcional | Cuándo |
|---:|---|---|
| `404` | `CREATURE_NOT_FOUND` | No existe criatura |
| `400` | `TEMPLATE_REQUIRED` | Falta plantilla obligatoria |
| `400` | `TEMPLATE_NOT_ALLOWED` | Plantilla no permitida |
| `400` | `UNSUPPORTED_SUMMON_LEVEL` | Nivel no soportado por la regla de cantidad |

---

# 4. Daño, curación y eliminación

## Validación común de `amount`

Los endpoints de daño y curación usan un campo `amount` con estas reglas:

| Campo | Tipo | Obligatorio | Restricción |
|---|---|---:|---|
| `amount` | integer | Sí | `minimum: 1` |

Valores inválidos:

- campo ausente;
- `null`;
- vacío;
- `0`;
- números negativos;
- decimales;
- texto u otros tipos no numéricos.

Si `amount` es inválido, la API no debe modificar PG, no debe cambiar el estado de la instancia, no debe persistir cambios y debe devolver `400 Bad Request` con el código funcional correspondiente.

El signo de la operación lo determina el endpoint:

- `/damage` resta `amount`;
- `/heal` suma `amount`;
- el cliente no debe enviar cantidades negativas.

Los botones rápidos de interfaz pueden mostrarse como `-1`, `-5`, `-10`, `+1`, `+5` o `+10`, pero el payload debe enviar siempre el valor positivo correspondiente, por ejemplo `{ "amount": 5 }`.

## POST `/api/combat-state/instances/{instanceId}/damage`

Aplica daño a una criatura individual.

### Path params

| Parámetro | Tipo | Descripción |
|---|---|---|
| `instanceId` | string | ID de instancia activa |

### Request body

```json
{
  "amount": 5
}
```

### Campos

| Campo | Tipo | Obligatorio | Restricción | Descripción |
|---|---|---:|---|---|
| `amount` | integer | Sí | `minimum: 1` | Daño final positivo que se restará a los PG de la instancia |

### Reglas

- `amount` representa el daño final que el usuario quiere aplicar como entero positivo mayor o igual que 1.
- La API no calcula RD, resistencias ni inmunidades.
- La API resta PG solo a esa instancia.
- Debe permitir PG a 0 o negativos.
- Si queda a 0 o menos, la instancia se marca como `DOWN`.
- Debe persistir estado.

### Respuesta `200 OK`

```json
{
  "instance": {
    "id": "instance-2",
    "displayNumber": 2,
    "displayName": "Fiery Badger 2",
    "currentHitPoints": 7,
    "maxHitPoints": 15,
    "status": "DAMAGED"
  },
  "combatState": {
    "groups": [],
    "dailyUses": {
      "maximum": 6,
      "remaining": 4
    }
  }
}
```

### Respuesta si cae

```json
{
  "instance": {
    "id": "instance-2",
    "displayNumber": 2,
    "displayName": "Fiery Badger 2",
    "currentHitPoints": -2,
    "maxHitPoints": 15,
    "status": "DOWN"
  },
  "combatState": {
    "groups": [],
    "dailyUses": {
      "maximum": 6,
      "remaining": 4
    }
  }
}
```

### Errores

| Código HTTP | Código funcional | Cuándo |
|---:|---|---|
| `404` | `INSTANCE_NOT_FOUND` | No existe la instancia |
| `400` | `INVALID_DAMAGE_AMOUNT` | `amount` ausente, `null`, vacío, `0`, negativo, decimal, texto o no numérico |

---

## POST `/api/combat-state/instances/{instanceId}/heal`

Aplica curación a una criatura individual.

### Request body

```json
{
  "amount": 5
}
```

### Campos

| Campo | Tipo | Obligatorio | Restricción | Descripción |
|---|---|---:|---|---|
| `amount` | integer | Sí | `minimum: 1` | Curación positiva que se sumará a los PG de la instancia |

### Reglas

- `amount` representa la curación como entero positivo mayor o igual que 1.
- La curación se aplica solo a la instancia seleccionada.
- En el MVP, la curación no debe superar PG máximos.
- Si la criatura estaba caída y queda con PG mayores que 0, deja de estar `DOWN`.
- Tras la curación, el estado se recalcula: `HEALTHY` si queda a PG máximos, `DAMAGED` si queda por encima de 0 y por debajo de PG máximos, `DOWN` si sigue a 0 PG o menos.
- Debe persistir estado.

### Respuesta `200 OK`

```json
{
  "instance": {
    "id": "instance-2",
    "displayNumber": 2,
    "displayName": "Fiery Badger 2",
    "currentHitPoints": 3,
    "maxHitPoints": 15,
    "status": "DAMAGED"
  },
  "combatState": {
    "groups": [],
    "dailyUses": {
      "maximum": 6,
      "remaining": 4
    }
  }
}
```

### Errores

| Código HTTP | Código funcional | Cuándo |
|---:|---|---|
| `404` | `INSTANCE_NOT_FOUND` | No existe la instancia |
| `400` | `INVALID_HEAL_AMOUNT` | `amount` ausente, `null`, vacío, `0`, negativo, decimal, texto o no numérico |

---

## DELETE `/api/combat-state/instances/{instanceId}`

Elimina una criatura individual activa.

### Reglas

- Elimina solo esa instancia.
- No afecta a otras instancias del mismo grupo.
- Si el grupo queda vacío, se elimina el grupo.
- Debe persistir estado.
- El frontend debe pedir confirmación antes de llamar o la API puede aceptar una confirmación explícita en fase futura.

### Respuesta `200 OK`

```json
{
  "removedInstanceId": "instance-2",
  "removedGroupId": null,
  "combatState": {
    "groups": [],
    "dailyUses": {
      "maximum": 6,
      "remaining": 4
    }
  }
}
```

### Respuesta si el grupo queda vacío

```json
{
  "removedInstanceId": "instance-2",
  "removedGroupId": "group-1",
  "combatState": {
    "groups": [],
    "dailyUses": {
      "maximum": 6,
      "remaining": 4
    }
  }
}
```

---

## DELETE `/api/combat-state/summons`

Limpia todas las invocaciones activas.

### Reglas

- Elimina todos los grupos.
- Elimina todas las instancias.
- Conserva configuración.
- Conserva usos diarios.
- Conserva últimas usadas y más usadas.
- Debe persistir estado.
- El frontend debe pedir confirmación antes de llamar.

### Respuesta `200 OK`

```json
{
  "removedGroups": 2,
  "removedInstances": 8,
  "combatState": {
    "groups": [],
    "dailyUses": {
      "maximum": 6,
      "remaining": 4
    },
    "lastRollResult": null
  }
}
```

---

# 5. Tiradas

## POST `/api/combat-state/groups/{groupId}/roll-attacks`

Tira todos los ataques de todas las criaturas de un grupo.

### Path params

| Parámetro | Tipo | Descripción |
|---|---|---|
| `groupId` | string | ID del grupo activo |

### Request body

No requiere cuerpo.

Puede enviarse `{}` si el cliente lo necesita.

### Reglas

- Tira todos los ataques de cada instancia del grupo.
- Respeta `quantity`.
- Tira daño junto al ataque.
- Muestra daño como `Daño si impacta`.
- Mantiene daños separados por tipo.
- Detecta amenaza de crítico.
- Tira confirmación.
- Tira daño crítico.
- Muestra daño normal aunque haya crítico.
- No decide impacto.
- No decide si el crítico confirma.
- No aplica daño automáticamente.
- Actualiza `lastRollResult`.

### Respuesta `200 OK`

```json
{
  "rollResult": {
    "id": "roll-attack-1",
    "type": "ATTACK_GROUP",
"title": "Atacar: Fiery Badger",
    "groupId": "group-1",
    "creatureName": "Fiery Badger",
    "createdAt": "2026-06-06T18:45:00Z",
    "instanceResults": [
      {
        "instanceId": "instance-1",
        "instanceDisplayName": "Fiery Badger 1",
        "attackResults": [
          {
            "attackId": "bite",
            "attackName": "Bite",
            "attackIndex": null,
            "attackRoll": {
              "formula": "1d20+4",
              "naturalResults": [14],
              "modifier": 4,
              "total": 18,
              "label": "Bite"
            },
            "normalDamage": {
              "components": [
                {
                  "formula": "1d3+3",
                  "roll": {
                    "formula": "1d3+3",
                    "naturalResults": [2],
                    "modifier": 3,
                    "total": 5,
                    "label": "piercing"
                  },
                  "damageType": "PIERCING",
                  "multipliesOnCritical": true,
                  "total": 5
                },
                {
                  "formula": "1",
                  "roll": {
                    "formula": "1",
                    "naturalResults": [],
                    "modifier": 1,
                    "total": 1,
                    "label": "fire"
                  },
                  "damageType": "FIRE",
                  "multipliesOnCritical": false,
                  "total": 1
                }
              ],
              "total": 6,
              "displayText": "1d3+3 = 5 piercing + 1 fire"
            },
            "criticalThreat": null,
            "displayText": "Bite: d20 14 + 4 = 18 | Daño si impacta: 5 piercing + 1 fire"
          },
          {
            "attackId": "claw",
            "attackName": "Claw",
            "attackIndex": 1,
            "attackRoll": {
              "formula": "1d20+4",
              "naturalResults": [20],
              "modifier": 4,
              "total": 24,
              "label": "Claw 1"
            },
            "normalDamage": {
              "components": [
                {
                  "formula": "1d2+3",
                  "roll": {
                    "formula": "1d2+3",
                    "naturalResults": [1],
                    "modifier": 3,
                    "total": 4,
                    "label": "slashing"
                  },
                  "damageType": "SLASHING",
                  "multipliesOnCritical": true,
                  "total": 4
                }
              ],
              "total": 4,
              "displayText": "1d2+3 = 4 slashing"
            },
            "criticalThreat": {
              "confirmationRoll": {
                "formula": "1d20+4",
                "naturalResults": [13],
                "modifier": 4,
                "total": 17,
                "label": "Confirmación Claw 1"
              },
              "criticalDamage": {
                "components": [
                  {
                    "formula": "2d2+6",
                    "roll": {
                      "formula": "2d2+6",
                      "naturalResults": [1, 2],
                      "modifier": 6,
                      "total": 9,
                      "label": "slashing crítico"
                    },
                    "damageType": "SLASHING",
                    "multipliesOnCritical": true,
                    "total": 9
                  }
                ],
                "total": 9,
                "displayText": "2d2+6 = 9 slashing"
              }
            },
            "displayText": "Claw 1 amenaza: d20 20 + 4 = 24 | Confirmación: d20 13 + 4 = 17 | Daño normal: 4 slashing | Daño crítico: 9 slashing"
          }
        ]
      }
    ]
  },
  "combatState": {
    "groups": [],
    "lastRollResult": {
      "id": "roll-attack-1",
      "type": "ATTACK_GROUP",
"title": "Atacar: Fiery Badger",
      "createdAt": "2026-06-06T18:45:00Z",
      "content": "Fiery Badger 1\nBite: d20 14 + 4 = 18..."
    }
  }
}
```

### Nota sobre `displayText`

La API puede devolver `displayText` para facilitar una UI rápida.

El frontend puede usar la estructura detallada si quiere renderizar con más control.

### Errores

| Código HTTP | Código funcional | Cuándo |
|---:|---|---|
| `404` | `GROUP_NOT_FOUND` | No existe el grupo |
| `409` | `GROUP_EMPTY` | El grupo no tiene instancias |

---

## POST `/api/combat-state/groups/{groupId}/roll-saving-throws`

Tira Fortaleza, Reflejos y Voluntad para todas las criaturas de un grupo.

### Reglas

- Tira siempre las tres TS.
- No hay endpoint separado para Fort/Ref/Vol en el MVP.
- No resuelve consecuencias.
- Actualiza `lastRollResult`.

### Respuesta `200 OK`

```json
{
  "rollResult": {
    "id": "roll-ts-1",
    "type": "SAVING_THROWS_GROUP",
"title": "Salvaciones: Fiery Badger",
    "groupId": "group-1",
    "creatureName": "Fiery Badger",
    "createdAt": "2026-06-06T18:45:00Z",
    "instanceResults": [
      {
        "instanceId": "instance-1",
        "instanceDisplayName": "Fiery Badger 1",
        "fortitude": {
          "formula": "1d20+8",
          "naturalResults": [12],
          "modifier": 8,
          "total": 20,
          "label": "Fortaleza"
        },
        "reflex": {
          "formula": "1d20+3",
          "naturalResults": [7],
          "modifier": 3,
          "total": 10,
          "label": "Reflejos"
        },
        "will": {
          "formula": "1d20+1",
          "naturalResults": [15],
          "modifier": 1,
          "total": 16,
          "label": "Voluntad"
        },
        "displayText": "Fiery Badger 1: Fort 20 / Ref 10 / Will 16"
      }
    ],
    "displayText": "Fiery Badger 1: Fort 20 / Ref 10 / Will 16"
  },
  "combatState": {
    "groups": [],
    "lastRollResult": {
      "id": "roll-ts-1",
      "type": "SAVING_THROWS_GROUP",
"title": "Salvaciones: Fiery Badger",
      "createdAt": "2026-06-06T18:45:00Z",
      "content": "Fiery Badger 1: Fort 20 / Ref 10 / Will 16"
    }
  }
}
```

---

# 6. Ficha expandida

## GET `/api/combat-state/groups/{groupId}/expanded-stat-block`

Devuelve la ficha completa de la criatura final de un grupo activo.

### Reglas

- Debe devolver la criatura final transformada.
- No debe devolver la criatura base sin modificar como ficha principal.
- El campo técnico del texto completo debe llamarse `fullStatBlock`.
- `fullStatBlock` debe corresponder a la ficha final transformada si se puede generar.
- Si no se puede transformar perfectamente el texto libre, la respuesta debe devolver el texto final disponible o una estructura expandida construida desde campos finales, sin presentar la ficha base como ficha principal.
- Debe incluir cambios derivados de `Augment Summoning`, plantilla y `Deep Guardian`.
- Puede incluir texto completo, habilidades completas y estructura final.

### Respuesta `200 OK`

```json
{
  "groupId": "group-1",
  "resolvedCreatureId": "badger:FIERY:AUGMENT_SUMMONING:DEEP_GUARDIAN",
  "displayName": "Fiery Badger",
  "fullStatBlock": "Fiery Badger\nN Small animal\nInit +1; Senses low-light vision, scent; Perception +5\n...",
  "expandedAbilities": [
    {
      "name": "Blood rage",
      "text": "Texto completo o suficientemente detallado de la habilidad."
    }
  ],
  "appliedRules": [
    {
      "type": "AUGMENT_SUMMONING",
      "description": "+4 Str, +4 Con"
    },
    {
      "type": "VERSATILE_SUMMON_MONSTER",
      "description": "Fiery template"
    },
    {
      "type": "DEEP_GUARDIAN",
      "description": "+1 attack, +1 AC"
    }
  ]
}
```

### Nota

Este endpoint es opcional si `GET /api/combat-state` ya devuelve toda la ficha expandida.

Se recomienda mantenerlo si la ficha completa es larga y se quiere aligerar la pantalla principal.

---

# 7. Usos diarios

## Reglas funcionales de `DailyUses`

El contador de usos diarios debe cumplir siempre estas invariantes:

```text
maximum >= 0
0 <= remaining <= maximum
```

Reglas de comportamiento:

- `maximum` es un entero mayor o igual que 0.
- `remaining` es un entero mayor o igual que 0.
- `remaining` no puede superar `maximum`.
- Al invocar, si `remaining > 0`, la API resta 1 uso.
- Al invocar con `remaining = 0`, el MVP no bloquea rígidamente la invocación, pero mantiene `remaining = 0`.
- Sumar usos diarios no puede dejar `remaining > maximum`.
- Restar usos diarios no puede dejar `remaining < 0`.
- Resetear usos diarios deja `remaining = maximum`.
- Al actualizar `dailyUsesMaximum`, si el nuevo máximo es menor que el `remaining` actual, la API ajusta `remaining = min(remaining, maximum)` para mantener un estado cómodo en mesa.

La API no debe persistir estados con `remaining < 0` ni con `remaining > maximum`.

`INVALID_DAILY_USES` se devuelve cuando una petición directa de configuración intenta establecer valores incoherentes no ajustables o tipos inválidos, por ejemplo `maximum < 0`, `remaining < 0`, `remaining > maximum` si ambos valores se envían explícitamente de forma incoherente, o valores no enteros. Las cantidades inválidas en `/api/daily-uses/increase` y `/api/daily-uses/decrease` usan la validación de `amount` positivo y deben devolver `INVALID_DAILY_USES`.

## GET `/api/configuration`

Obtiene la configuración funcional.

### Respuesta `200 OK`

```json
{
  "maxSummonMonsterLevel": 3,
  "dailyUses": {
    "maximum": 6,
    "remaining": 4
  },
  "availableTemplates": ["CHTHONIC", "FIERY", "CELESTIAL", "ENTROPIC", "RESOLUTE"],
  "enabledFixedRules": [
    "AUGMENT_SUMMONING",
    "SUPERIOR_SUMMONING",
    "VERSATILE_SUMMON_MONSTER",
    "DEEP_GUARDIAN"
  ]
}
```

---

## PATCH `/api/configuration`

Actualiza configuración básica.

### Request body

```json
{
  "maxSummonMonsterLevel": 3,
  "dailyUsesMaximum": 6,
  "dailyUsesRemaining": 4
}
```

### Reglas

- Debe persistir configuración.
- Puede aceptar actualización parcial.
- No debe activar sistemas fuera de MVP.
- `dailyUsesMaximum`, si se envía, debe ser un entero `>= 0`.
- `dailyUsesRemaining`, si se envía, debe ser un entero `>= 0`.
- Si se envían ambos, debe cumplirse `dailyUsesRemaining <= dailyUsesMaximum`; si no se cumple, la API debe devolver `INVALID_DAILY_USES`.
- Si solo se reduce `dailyUsesMaximum` y el `remaining` actual queda por encima del nuevo máximo, la API debe ajustar `remaining = min(remaining, dailyUsesMaximum)`.
- Si se envía `dailyUsesRemaining` por encima del máximo efectivo, la API debe devolver `INVALID_DAILY_USES`.

### Respuesta `200 OK`

```json
{
  "maxSummonMonsterLevel": 3,
  "dailyUses": {
    "maximum": 6,
    "remaining": 4
  },
  "availableTemplates": ["CHTHONIC", "FIERY", "CELESTIAL", "ENTROPIC", "RESOLUTE"],
  "enabledFixedRules": [
    "AUGMENT_SUMMONING",
    "SUPERIOR_SUMMONING",
    "VERSATILE_SUMMON_MONSTER",
    "DEEP_GUARDIAN"
  ]
}
```

---

## POST `/api/daily-uses/increase`

Suma usos diarios manualmente.

### Request body

```json
{
  "amount": 1
}
```

`amount` reutiliza la misma regla estructural: entero positivo obligatorio mayor o igual que 1.

### Reglas

- Suma `amount` a `remaining`.
- El resultado se acota a `maximum`.
- Si `remaining = maximum`, sumar usos mantiene `remaining = maximum`.
- No debe persistir `remaining > maximum`.
- Si `amount` es inválido, devuelve `400 Bad Request` con `INVALID_DAILY_USES`.

### Respuesta `200 OK`

```json
{
  "dailyUses": {
    "maximum": 6,
    "remaining": 5
  },
  "combatState": {
    "groups": []
  }
}
```

---

## POST `/api/daily-uses/decrease`

Resta usos diarios manualmente.

### Request body

```json
{
  "amount": 1
}
```

`amount` reutiliza la misma regla estructural: entero positivo obligatorio mayor o igual que 1. El endpoint determina que ese valor se resta a los usos diarios.

### Reglas

- Resta `amount` a `remaining`.
- El resultado se acota a 0.
- Si `remaining = 0`, restar usos mantiene `remaining = 0`.
- No debe persistir `remaining < 0`.
- Debe persistir estado.
- Si `amount` es inválido, devuelve `400 Bad Request` con `INVALID_DAILY_USES`.

### Respuesta `200 OK`

```json
{
  "dailyUses": {
    "maximum": 6,
    "remaining": 3
  },
  "combatState": {
    "groups": []
  }
}
```

---

## POST `/api/daily-uses/reset`

Resetea usos diarios a su máximo.

### Request body

Puede omitirse o enviarse vacío:

```json
{}
```

### Reglas

- Establece `remaining = maximum`.
- No modifica `maximum`.
- Debe persistir estado.

### Respuesta `200 OK`

```json
{
  "dailyUses": {
    "maximum": 6,
    "remaining": 6
  },
  "combatState": {
    "groups": []
  }
}
```

---

# 8. DTOs principales

Esta sección resume los DTOs que deberían derivarse para `openapi.yaml`.

---

## CombatStateDto

```json
{
  "groups": [],
  "dailyUses": {},
  "configuration": {},
  "lastRollResult": {},
  "recentlyUsedSummons": [],
  "mostUsedSummons": []
}
```

### Campos

| Campo | Tipo | Descripción |
|---|---|---|
| `groups` | `ActiveSummonGroupDto[]` | Grupos activos |
| `dailyUses` | `DailyUsesDto` | Usos diarios |
| `configuration` | `ConfigurationDto` | Configuración |
| `lastRollResult` | `RollDisplayDto/null` | Último resultado |
| `recentlyUsedSummons` | `SummonShortcutDto[]` | Últimas usadas |
| `mostUsedSummons` | `SummonShortcutDto[]` | Más usadas |

---

## ActiveSummonGroupDto

```json
{
  "id": "group-1",
  "resolvedCreature": {},
  "instances": []
}
```

---

## ActiveSummonInstanceDto

```json
{
  "id": "instance-1",
  "displayNumber": 1,
  "displayName": "Fiery Badger 1",
  "currentHitPoints": 15,
  "maxHitPoints": 15,
  "status": "HEALTHY"
}
```

### `status`

Valores visibles y de API:

```text
HEALTHY
DAMAGED
DOWN
```

Reglas de cálculo:

- `HEALTHY`: `currentHitPoints == maxHitPoints`.
- `DAMAGED`: `currentHitPoints > 0 && currentHitPoints < maxHitPoints`.
- `DOWN`: `currentHitPoints <= 0`.

`REMOVED` no debe exponerse ni persistirse como estado. Si una instancia se elimina, desaparece del grupo. Si el grupo se queda sin instancias, desaparece del estado de combate.

`ACTIVE` no debe usarse como estado de instancia porque es ambiguo frente a `HEALTHY` y `DAMAGED`.

---

## ResolvedCreatureDto

Representa criatura final.

Campos recomendados:

```json
{
  "id": "badger:FIERY:AUGMENT_SUMMONING:DEEP_GUARDIAN",
  "baseTemplateId": "badger",
  "displayName": "Fiery Badger",
  "summonLevel": 1,
  "appliedTemplate": "FIERY",
  "alignment": "N",
  "size": "SMALL",
  "creatureType": "animal",
  "subtypes": [],
  "initiative": 1,
  "senses": [],
  "perception": 5,
  "armorClass": {},
  "maxHitPoints": 15,
  "savingThrows": {},
  "speeds": [],
  "speedsText": "Speed 30 ft., burrow 10 ft.",
  "attacks": [],
  "attacksText": "Melee bite +4 (1d3+3 + 1 fire)",
  "space": "5 ft.",
  "reach": "5 ft.",
  "specialAttacks": [],
  "specialDefenses": [],
  "shortAbilities": [],
  "expandedAbilities": [],
  "fullStatBlock": "",
  "appliedRules": []
}
```

---

## DamageCreatureRequestDto

```json
{
  "amount": 5
}
```

---

## HealCreatureRequestDto

```json
{
  "amount": 5
}
```

---

## SummonCreatureRequestDto

```json
{
  "creatureTemplateId": "badger",
  "selectedTemplate": "FIERY",
  "source": "MANUAL_SEARCH"
}
```

---

## DiceRollDto

```json
{
  "formula": "1d20+4",
  "naturalResults": [14],
  "modifier": 4,
  "total": 18,
  "label": "Bite"
}
```

---

## RollDisplayDto

```json
{
  "id": "roll-1",
  "type": "ATTACK_GROUP",
  "title": "Atacar con todas: Fiery Badger",
  "createdAt": "2026-06-06T18:45:00Z",
  "content": "Fiery Badger 1\nBite: d20 14 + 4 = 18..."
}
```

---

# 9. Enumeraciones

## SummonTemplateType

```text
CHTHONIC
FIERY
CELESTIAL
ENTROPIC
RESOLUTE
```

## SummonSelectionSource

```text
MANUAL_SEARCH
RECENT
MOST_USED
FAVORITE
```

## InstanceStatus

```text
HEALTHY
DAMAGED
DOWN
```

No existen otros estados de instancia en el contrato REST del MVP.

- `HEALTHY`: `currentHitPoints == maxHitPoints`.
- `DAMAGED`: `currentHitPoints > 0 && currentHitPoints < maxHitPoints`.
- `DOWN`: `currentHitPoints <= 0`.

`REMOVED` no se modela como estado: eliminar una criatura borra la instancia activa.

## RollDisplayType

```text
ATTACK_GROUP
SAVING_THROWS_GROUP
SUMMON_QUANTITY
GENERIC
```

## AttackType

```text
MELEE
RANGED
TOUCH
SPECIAL
```

## DamageType

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

## SpecialDefenseType

```text
DAMAGE_REDUCTION
RESISTANCE
IMMUNITY
SPELL_RESISTANCE
VULNERABILITY
OTHER
```

## SpeedType

```text
LAND
FLY
SWIM
CLIMB
BURROW
OTHER
```

---

# 10. Decisiones importantes

## La API devuelve estado actualizado tras acciones

Para facilitar la rapidez en mesa, las acciones principales devuelven el estado actualizado o una parte suficiente del estado.

Aplica a:

- invocar;
- dañar;
- curar;
- eliminar;
- limpiar invocaciones;
- tirar ataques;
- tirar TS;
- modificar usos diarios.

## Las tiradas devuelven estructura y texto

Los resultados de tirada pueden devolver:

- estructura detallada para UI rica;
- `displayText` o `content` para renderizado rápido.

Esto evita que el frontend tenga que recomponer todas las frases.

## La ficha expandida puede ir aparte

`GET /api/combat-state` puede devolver ficha resumida y datos esenciales.

La ficha completa puede solicitarse con:

```text
GET /api/combat-state/groups/{groupId}/expanded-stat-block
```

Esto evita respuestas demasiado grandes en la pantalla principal.

## La API no tiene autenticación

No hay login ni seguridad en el MVP.

La aplicación es local y personal.

## La API no tiene multiusuario

No hay usuario propietario en los endpoints.

No hay perfiles.

No hay permisos.

---

# 11. Operaciones fuera del MVP

No crear endpoints para:

- enemigos;
- iniciativa;
- turnos;
- mapa;
- posicionamiento;
- duración por asaltos;
- estados completos;
- buffs/debuffs;
- modificadores temporales;
- objetivos de ataques;
- aplicar daño a enemigos;
- comprobar impacto contra CA;
- resolver críticos contra CA;
- hechizos de criaturas;
- auras;
- integración con Fantasy Grounds;
- login;
- usuarios;
- roles;
- permisos.

---

# 12. Criterios de aceptación de la API

La API REST del MVP se considera suficiente cuando permite:

1. Obtener el estado completo de combate.
2. Listar criaturas del catálogo.
3. Buscar criaturas del catálogo.
4. Obtener detalle de criatura base.
5. Previsualizar criatura final con plantilla.
6. Invocar criatura desde JSON.
7. Calcular cantidad automáticamente.
8. Aplicar reglas fijas al invocar.
9. Crear instancias separadas.
10. Agrupar por criatura final.
11. Descontar usos diarios al invocar.
12. Ajustar PG de una instancia individual.
13. Ajustar PG de una instancia individual.
14. Marcar criatura como caída a 0 PG o menos.
15. Eliminar instancia individual.
16. Limpiar todas las invocaciones.
17. Tirar ataques de todo un grupo.
18. Mostrar dado, modificador y total.
19. Mostrar daño como `Daño si impacta`.
20. Mostrar daños separados por tipo.
21. Gestionar amenaza de crítico, confirmación, daño normal y daño crítico.
22. Tirar Fortaleza, Reflejos y Voluntad de todo un grupo.
23. Mostrar resultado más reciente.
24. Persistir estado tras cambios.
25. Recuperar estado al recargar.

