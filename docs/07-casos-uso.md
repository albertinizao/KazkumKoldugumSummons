# Casos de uso actuales

## 1. Invocar una criatura desde selección manual

1. Ir a **Invocaciones**.
2. Filtrar o buscar la criatura.
3. Elegir plantilla si la criatura lo requiere.
4. Pulsar `Invocar`.
5. Ver el grupo nuevo en **Combate**.

## 2. Invocar desde últimas usadas o más usadas

1. Ir a **Invocaciones**.
2. Pulsar un acceso rápido.
3. El backend reutiliza el `shortcutId` y la plantilla asociada.
4. El estado de combate se actualiza y se persiste.

## 3. Gestionar PG de una criatura

1. Ir a **Combate**.
2. Abrir la card de la instancia.
3. Usar un botón rápido o `Otra cantidad`.
4. Aplicar daño o curación.
5. Ver el estado `HEALTHY`, `DAMAGED` o `DOWN`.

## 4. Resolver ataques o TS de un grupo

1. Ir a **Combate**.
2. Pulsar `Atacar` o `Salvaciones` en el grupo.
3. Revisar el modal con el resultado.

## 5. Limpiar toda la mesa de invocaciones

1. Pulsar `Limpiar` en la barra superior.
2. Confirmar la acción.
3. El estado de combate queda vacío.

## 6. Ajustar configuración

1. Ir a **Configuración**.
2. Cambiar `maxSummonMonsterLevel` y/o usos diarios máximos.
3. Guardar.
4. El combate refresca su configuración.

## 7. Revisar el catálogo antes de invocar

1. Ir a **Catálogo**.
2. Buscar una criatura.
3. Comparar la ficha base con la ficha final resuelta.
4. Invocar con más contexto.
