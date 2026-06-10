# Alcance funcional actual

Este documento refleja el comportamiento que el código implementa hoy.

## Incluido

### Combate

- ver grupos de invocaciones activas;
- atacar con todas las criaturas de un grupo;
- tirar TS de todas las criaturas de un grupo;
- tirar ataques y TS de todos los grupos desde la vista de combate;
- ver el último resultado de tirada en modal;
- expandir la ficha final completa de un grupo;
- limpiar todas las invocaciones activas con confirmación.

### Invocaciones

- seleccionar criatura base desde catálogo;
- filtrar por nivel de invocación;
- elegir plantilla cuando corresponde;
- invocar desde selección manual;
- invocar desde últimas usadas;
- invocar desde más usadas;
- abrir el asistente de invocación;
- mostrar un toast con la cantidad invocada.

### Catálogo

- buscar criaturas por texto;
- filtrar por nivel exacto;
- filtrar por nivel máximo;
- filtrar por plantilla en la UI;
- previsualizar criatura base y criatura final;
- ver resumen mecánico y stat block completo.

### Configuración

- modificar `maxSummonMonsterLevel`;
- modificar el máximo de usos diarios;
- guardar y recargar configuración.

### Persistencia

- estado de combate en H2;
- configuración en H2;
- snapshot de arranque en `localStorage`;
- recientes y más usadas persistidos en el estado de combate;
- última tirada persistida como parte del estado.

## Fuera de alcance

El código actual no implementa:

- login;
- multijugador;
- enemigos;
- iniciativa;
- mapa táctico;
- posicionamiento;
- duración por asaltos;
- automatización de impactos;
- automatización de críticos sobre la CA enemiga;
- automatización de RD/resistencias/inmunidades sobre el objetivo;
- buffs/debuffs contextuales;
- sistema completo de condiciones.

## Observación importante

La UI actual tiene una forma concreta de uso:

- la acción global de limpiar invocaciones está en la barra superior;
- las cartas de criatura muestran PG y botones rápidos `-10`, `-5`, `-1`, `+1`, `+5`, `+10`;
- la cantidad libre se resuelve en un modal con acciones de **Dañar** y **Curar**;
- la eliminación individual es directa, sin confirmación.
