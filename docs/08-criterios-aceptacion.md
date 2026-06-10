# Criterios de aceptación actuales

## Combate

- La vista de combate muestra grupos activos y cards individuales.
- Un grupo puede tirar ataques y TS.
- Existen acciones globales para todos los grupos.
- El último resultado de tirada se puede consultar en modal.
- La ficha expandida muestra la criatura final, no la base.

## Invocaciones

- La app permite invocar desde el catálogo manual.
- La app permite invocar desde accesos rápidos.
- La cantidad se calcula automáticamente según la diferencia de nivel.
- Se persiste el estado tras invocar.

## PG

- Cada instancia tiene PG independientes.
- Se puede dañar.
- Se puede curar.
- Si baja a 0 o menos, queda en estado `DOWN`.
- Si se cura por encima de 0, vuelve a `DAMAGED` o `HEALTHY`.

## Catálogo

- El catálogo permite buscar y filtrar.
- La previsualización muestra la criatura final resuelta.
- La ficha base y la ficha final se distinguen claramente.

## Configuración y persistencia

- `maxSummonMonsterLevel` se puede actualizar.
- Los usos diarios máximos se pueden actualizar.
- El estado se restaura al recargar.
- Los accesos recientes y más usados sobreviven entre acciones.

## Límites conocidos del código actual

- La eliminación individual no pide confirmación.
- El filtrado por plantilla del catálogo todavía no es semánticamente completo en el listado.
- No existe un endpoint separado para la ficha expandida: el stat block final ya viene dentro de `ResolvedCreature`.
