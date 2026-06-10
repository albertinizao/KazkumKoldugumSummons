# Catálogo de criaturas

## Fuente de verdad

Las criaturas base viven en JSON manuales bajo `src/main/resources/catalog/`.

El backend las carga a través de `JsonCreatureTemplateRepository` y expone el catálogo por REST.

## Qué devuelve el backend

### `GET /api/catalog/creatures`

Soporta:

- `query`
- `summonLevel`
- `maxSummonLevel`
- `template`
- `limit`
- `offset`

Devuelve:

- `items`
- `total`

Cada item incluye:

- `id`
- `name`
- `summonLevel`
- `alignment`
- `size`
- `creatureType`
- `subtypes`
- `allowedTemplates`
- `summary`

El `summary` actual contiene:

- `armorClass`
- `maxHitPoints`
- `savingThrows`
- `speedsText`
- `attacksText`

## Detalle y previsualización

### `GET /api/catalog/creatures/{creatureTemplateId}`

Devuelve la ficha base completa.

### `GET /api/catalog/creatures/{creatureTemplateId}/resolved-preview`

Devuelve la criatura final resuelta con las reglas actuales:

- Augment Summoning;
- plantilla opcional;
- Deep Guardian cuando aplica.

## Comportamiento actual importante

La UI de catálogo filtra por plantilla, pero el backend actual solo usa el parámetro `template` para excluir outsiders en la búsqueda; la previsualización sí resuelve la plantilla real.

Ese detalle debe conocerse porque el catálogo no hace todavía un filtrado semántico completo por plantilla en el listado.

## Uso en la UI

La vista de catálogo usa esta información para:

- buscar y filtrar criaturas;
- comparar ficha base y ficha final;
- comprobar si una criatura acepta plantillas;
- inspeccionar defensas, ataques y stat block completo antes de invocar.
