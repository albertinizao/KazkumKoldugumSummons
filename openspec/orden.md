## Orden recomendado de implementación

### 1. `summoner-configuration`
Primero necesitas la base de configuración:
- `maxSummonMonsterLevel`
- persistencia de configuración
- valores por defecto

**Por qué primero:** todo lo demás depende de ese nivel máximo.

---

### 2. `creature-catalog`
Después, el catálogo base de criaturas:
- carga JSON
- búsqueda
- filtro
- detalle de criatura base

**Por qué ahora:** sin catálogo no hay nada que invocar.

---

### 3. `summon-templates`
Luego las plantillas:
- Chthonic
- Fiery
- Celestial
- Entropic
- Resolute
- bonus por `earth` o `burrow`

**Por qué aquí:** estas reglas alteran la criatura final. Si no las resuelves antes, luego tendrás que rehacer la lógica de criatura final y de ataques.

---

### 4. `creature-resolution`
Aquí construyes la criatura final:
- Augment Summoning
- Versatile Summon Monster
- Deep Guardian
- bonus adicional por `earth/burrow`
- identidad final del grupo

**Por qué después de templates:** porque la resolución depende de ellas.  
**Importante:** este punto ya queda afectado por la nueva especificación de plantillas.

---

### 5. `daily-uses`
Después implementa el contador diario:
- máximo
- remaining
- increase/decrease/reset
- límites

**Por qué aquí:** afecta al flujo de invocación, pero no al motor de resolución.

---

### 6. `combat-state-management`
Ahora sí, el modelo de combate activo:
- grupos
- instancias
- PG por instancia
- HEALTHY / DAMAGED / DOWN
- eliminación individual
- limpiar todo

**Por qué aquí:** ya tienes criatura base/final y puedes persistir el estado real.

---

### 7. `summoning`
Con todo lo anterior, implementas el acto de invocar:
- abrir flujo
- seleccionar criatura
- aplicar plantilla
- calcular cantidad
- descontar usos
- crear grupo/instancias
- persistir

**Por qué aquí:** esta es la primera funcionalidad “end-to-end”.

---

### 8. `combat-rolls`
Luego las tiradas:
- ataques globales
- daño por tipo
- críticos
- TS
- daño normal y crítico

**Por qué después:** las tiradas usan la criatura final ya resuelta.

---

### 9. `persistence`
Si no la has hecho ya de forma transversal, consolidar aquí:
- guardar/restaurar combate
- recarga de estado
- recent/most-used summons

**Por qué aquí:** la persistencia debe existir desde el inicio, pero esta spec es la que te obliga a cerrarla bien.

---

### 10. `combat-screen`
Finalmente la pantalla principal:
- Invocar
- Limpiar invocaciones
- contador
- grupos
- cards
- último resultado
- responsive/dark mode

**Por qué al final:** la UI debe consumir una lógica ya estable, no definirla.

---

## Orden resumido, corto y práctico

1. `summoner-configuration`
2. `creature-catalog`
3. `summon-templates`
4. `creature-resolution`
5. `daily-uses`
6. `combat-state-management`
7. `summoning`
8. `combat-rolls`
9. `persistence`
10. `combat-screen`

---

## Rcomendación
Si se va a implementar por fases reales de trabajo:

- **Fase 1:** `summoner-configuration` + `creature-catalog` + `creature-resolution`
- **Fase 2:** `summon-templates` + `summoning` + `combat-state-management`
- **Fase 3:** `daily-uses` + `combat-rolls` + `persistence`
- **Fase 4:** `combat-screen`

Eso te da una base sólida.  
Y te lo digo claro: **si empiezas por la pantalla, estás invirtiendo el orden de la arquitectura**. Primero resuelve el dominio; luego lo pintas.

Si quieres, te puedo dar ahora el **orden exacto de implementación en OpenSpec change-by-change**, incluyendo qué spec va antes y cuáles son deltas sobre otras.