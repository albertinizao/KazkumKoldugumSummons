# Reglas de tiradas

## Tiradas de cantidad de invocaciones

La fórmula depende de la diferencia entre el nivel máximo configurado y el nivel de la criatura:

| Diferencia | Fórmula | Máximo posible |
|---|---|---:|
| 0 o menos | `1` | 1 |
| 1 | `1d3+1` | 4 |
| 2 o más | `1d4+2` | 6 |

La app tira la cantidad automáticamente al invocar.

## Ataques por grupo

Al pulsar `Atacar` en un grupo:

- se recorren todas las instancias del grupo;
- se tiran todos los ataques de la criatura final;
- si un ataque tiene `quantity > 1`, se tiran ataques separados;
- el daño se muestra como `Daño si impacta`;
- no se decide si el ataque impacta al objetivo.

### Críticos

Si el ataque amenaza crítico, el resultado muestra:

- tirada de ataque;
- daño normal;
- tirada de confirmación;
- daño crítico.

### Formato de daño

Los componentes de daño se muestran separados por tipo:

```text
5 piercing + 1 fire
```

No se colapsan en un valor genérico.

## TS por grupo

Al pulsar `Salvaciones`:

- se tiran Fortaleza, Reflejos y Voluntad;
- se hace para cada instancia del grupo;
- el resultado se agrupa por criatura.

## Tiradas globales

En la vista de combate existen dos acciones globales:

- `Atacar con todas`
- `Tirar TS con todas`

Ambas recorren todos los grupos activos.

## Resultado visible

El sistema conserva el último resultado de tirada en el estado de combate para mostrarlo en modal.
