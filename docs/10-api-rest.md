# API REST actual

## Base

Todas las rutas cuelgan de:

```text
/api
```

## Formato

- `Content-Type: application/json`
- `Accept: application/json`

## Endpoints disponibles

### Catálogo

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/api/catalog/creatures` | Listar, buscar y filtrar criaturas base |
| `GET` | `/api/catalog/creatures/{creatureTemplateId}` | Obtener la ficha base completa |
| `GET` | `/api/catalog/creatures/{creatureTemplateId}/resolved-preview` | Previsualizar la criatura final |

Parámetros de listado:

- `query`
- `summonLevel`
- `maxSummonLevel`
- `template`
- `limit`
- `offset`

### Estado de combate

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/api/combat-state` | Obtener el estado completo actual |
| `POST` | `/api/combat-state/summons` | Invocar una criatura |
| `DELETE` | `/api/combat-state/summons` | Limpiar todas las invocaciones |
| `DELETE` | `/api/combat-state/instances/{instanceId}` | Eliminar una instancia |
| `POST` | `/api/combat-state/instances/{instanceId}/damage` | Dañar una instancia |
| `POST` | `/api/combat-state/instances/{instanceId}/heal` | Curar una instancia |
| `DELETE` | `/api/combat-state/last-roll-result` | Limpiar el último resultado de tirada |
| `POST` | `/api/combat-state/groups/{groupId}/roll-attacks` | Tirar ataques de un grupo |
| `POST` | `/api/combat-state/groups/{groupId}/roll-saving-throws` | Tirar TS de un grupo |

### Configuración

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/api/configuration` | Leer configuración actual |
| `PUT` | `/api/configuration` | Actualizar configuración |
| `GET` | `/api/summoner-configuration` | Alias de lectura equivalente |
| `PUT` | `/api/summoner-configuration` | Alias de escritura equivalente |

### Usos diarios

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/daily-uses/increase` | Sumar usos |
| `POST` | `/api/daily-uses/decrease` | Restar usos |
| `POST` | `/api/daily-uses/reset` | Resetear usos |

### Cantidad de invocación

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/api/summons/quantity` | Consultar disponibilidad y fórmula de cantidad |

## Modelos relevantes

### `CombatState`

Devuelve:

- `activeGroups`
- `dailyUses`
- `configuration`
- `lastRollResult`
- `recentlyUsedSummons`
- `mostUsedSummons`

### `ConfigurationResponse`

La respuesta de configuración incluye:

- `maxSummonMonsterLevel`
- `dailyUses`
- `availableTemplates`
- `enabledFixedRules`

### `DailyUsesMutationResponse`

Los endpoints de usos diarios devuelven:

- `dailyUses`
- `combatState`

### `QuantityResponse`

`GET /api/summons/quantity` devuelve:

- `available`
- `formula`
- `maximumPossibleQuantity`

### `SummonCreatureRequest`

```json
{
  "creatureTemplateId": "badger",
  "selectedTemplate": "FIERY",
  "shortcutId": null,
  "source": "MANUAL_SEARCH"
}
```

Current behavior:

- if `shortcutId` is present, the backend summons from the matching recent/popular shortcut;
- otherwise `creatureTemplateId` is required;
- `selectedTemplate` may be `null` when the creature has no template choice.

### `AmountRequest`

```json
{ "amount": 5 }
```

La validación actual exige:

- entero;
- `amount >= 1`.

## Respuestas de tiradas

### Ataques de grupo

`POST /api/combat-state/groups/{groupId}/roll-attacks`

Devuelve:

- `rollResult`
- `combatState`

### TS de grupo

`POST /api/combat-state/groups/{groupId}/roll-saving-throws`

Devuelve:

- `rollResult`
- `combatState`

### Resultado de tirada

Los resultados contienen texto listo para UI:

- tirada de ataque con total;
- daño si impacta;
- amenaza de crítico;
- confirmación;
- daño crítico;
- o las tres salvaciones por instancia.

## Notas de comportamiento

- El backend persiste el estado tras cada mutación relevante.
- El catálogo acepta `template`, pero el filtrado semántico completo por plantilla todavía no está terminado en el listado.
- No existe un endpoint separado de `expanded-stat-block`; la ficha expandida final ya se expone dentro de `ResolvedCreature.fullStatBlock`.
