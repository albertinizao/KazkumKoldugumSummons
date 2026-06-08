# Alcance funcional

Este documento define qué debe hacer la aplicación en sus primeras fases y qué queda explícitamente fuera del alcance inicial.

La prioridad del producto es que sea **jugable en mesa**, rápido de usar desde una **tablet Android** y suficientemente sencillo como para no entorpecer el combate.

---

# Fase 1 — MVP jugable en mesa

La Fase 1 debe entregar una versión mínima pero realmente útil durante una partida.

El objetivo de esta fase no es cubrir todas las reglas posibles de Pathfinder 1e, sino permitir gestionar de forma rápida las invocaciones activas del Inquisidor Monster Tactician.

## 1. Pantalla principal de combate

La aplicación debe tener una pantalla principal que actúe como centro de uso durante el combate.

Debe mostrar:

- botón `Invocar`;
- botón `Limpiar invocaciones`;
- botones globales `Atacar con todas` y `Tirar TS con todas`;
- contador de usos diarios;
- lista de grupos de criaturas activas;

La pantalla debe estar optimizada para uso en vertical desde tablet Android.

Debe seguir siendo usable en horizontal y desde navegador de escritorio, pero esa no es la experiencia prioritaria.

## 2. Invocar criaturas disponibles

La aplicación debe permitir invocar criaturas cargadas desde JSON.

El flujo mínimo de invocación debe ser:

1. Pulsar `Invocar`.
2. Abrir modal de selección.
3. Elegir criatura mediante:
   - últimas usadas;
   - más usadas;
   - búsqueda manual.
4. Si la criatura se selecciona manualmente y puede tener varias plantillas, preguntar qué plantilla aplicar.
5. Si la criatura se selecciona desde últimas usadas o más usadas, usar el identificador estable del shortcut elegido (`shortcutId`) para reutilizar directamente la combinación ya conocida de criatura + plantilla.
6. Calcular automáticamente cuántas criaturas aparecen.
7. Descontar automáticamente 1 uso diario si `remaining > 0`; si `remaining = 0`, no bajar a negativo.
8. Añadir las criaturas activas a la pantalla principal.
9. Persistir inmediatamente el estado.

El diagrama de preguntas para decidir qué invocar queda fuera del MVP.

## 3. Nivel máximo de Summon Monster

Debe existir una configuración que indique el nivel máximo actual de `Summon Monster` disponible para el personaje.

En la situación inicial prevista:

```text
maxSummonMonsterLevel = 3
```

La aplicación debe usar este valor para determinar qué criaturas se pueden invocar y qué tirada de cantidad corresponde.

Regla funcional:

- criatura del nivel máximo disponible: 1 criatura;
- criatura de nivel máximo - 1: `1d3 + 1` criaturas;
- criatura de nivel máximo - 2: `1d4 + 2` criaturas.

La aplicación debe hacer esta tirada automáticamente al invocar.

## 4. Aplicación de reglas fijas del personaje

La aplicación debe aplicar las reglas fijas del personaje sobre la ficha base almacenada en JSON.

### 4.1. Augment Summoning

Toda criatura invocada debe recibir:

```text
+4 Fuerza
+4 Constitución
```

La ficha base del JSON no tiene por qué venir ya aumentada.

### 4.2. Superior Summoning

Siempre que se lance un dado para determinar el número de criaturas invocadas, se suma 1 criatura adicional.

Esto ya está reflejado en las fórmulas funcionales:

```text
1d3 + 1
1d4 + 2
```

### 4.3. Versatile Summon Monster

Si la criatura lo permite, debe poder aplicarse una de las siguientes plantillas:

- `Chthonic`
- `Fiery`
- `Celestial`
- `Entropic`
- `Resolute`

La ficha JSON base no incluye estas plantillas aplicadas.

La aplicación debe generar la ficha final invocable aplicando la plantilla seleccionada.

### 4.4. Deep Guardian

Si la criatura final, tras aplicar plantilla o no aplicarla si no procede, cumple al menos una de estas condiciones:

- tiene velocidad de tipo `burrow`;
- es del subtipo `earth`;

entonces debe recibir:

```text
+1 al ataque
+1 a la CA normal y a la CA desprevenida
```

No modifica la CA de toque.

## 5. Grupos de criaturas activas por tipo

Las criaturas activas deben mostrarse agrupadas por tipo final de criatura.

Una criatura se considera del mismo grupo si coincide en:

- criatura base;
- plantilla aplicada;
- reglas fijas resultantes;
- ficha final transformada.

Si se invoca dos veces la misma criatura con la misma plantilla, se añade al grupo existente.

Ejemplo:

```text
Primer conjuro: 3 Fiery Badger
Segundo conjuro: 2 Fiery Badger

Resultado: 1 grupo de Fiery Badger con 5 criaturas individuales.
```

Si es la misma criatura base con distinta plantilla, debe mostrarse en grupos distintos.

Ejemplo:

```text
Fiery Badger
Celestial Badger
Chthonic Badger
```

## 6. Información visible por grupo

Cada grupo de criaturas activas debe mostrar una ficha resumida.

Debe incluir, como mínimo:

- nombre final de la criatura;
- alineamiento, tamaño y tipo con subtipos visibles cuando existan;
- iniciativa;
- sentidos;
- percepción;
- CA;
- CA de toque;
- CA desprevenida;
- PG máximos;
- Fortaleza;
- Reflejos;
- Voluntad;
- velocidades;
- ataques básicos;
- daño de los ataques;
- espacio;
- alcance;
- ataques especiales;
- número de criaturas activas de ese tipo.

No debe incluir notas tácticas en la ficha resumida.

## 7. Cards individuales de criatura

Dentro de cada grupo, cada criatura activa debe mostrarse como una card individual.

Cada card debe mostrar:

- nombre visible de la criatura dentro del grupo;
- PG actuales;
- PG máximos;
- botones rápidos `-10`, `-5`, `-1`, `+1`, `+5`, `+10`;
- campo libre para introducir una cantidad con signo;
- botón `Eliminar`.

El botón `Eliminar` debe estar visible siempre.

Si una criatura queda a 0 PG o menos, la card debe indicarlo visualmente, pero no debe eliminarla automáticamente.

Algunas criaturas pueden seguir actuando o existiendo por debajo de 0 PG, así que la aplicación debe permitir seguir curándolas, dañándolas o eliminándolas manualmente.

## 8. Aplicar daño y curación

La aplicación debe permitir aplicar daño o curación a una criatura individual.

La acción debe resolverse con botones rápidos y un campo libre.

Reglas de interfaz:

- los valores negativos significan daño;
- los valores positivos significan curación;
- el campo libre puede aceptar cantidades con signo;
- la aplicación no debe aplicar automáticamente reducción de daño, resistencias ni inmunidades;
- si existen defensas especiales, deben mostrarse como referencia, pero no se aplican solas.

## 9. Eliminar criaturas

La aplicación debe permitir eliminar criaturas individuales.

Casos de uso:

- criatura muerta;
- criatura retirada;
- criatura que deja de ser relevante;
- corrección manual de la mesa.

La aplicación también debe incluir un botón general `Limpiar invocaciones` para eliminar todas las invocaciones activas.

Este botón sirve para cerrar un combate o limpiar la mesa entre escenas.

## 10. Tirar ataques de todas las criaturas de un tipo

Cada grupo de criaturas debe tener un botón:

```text
Atacar
```

Al pulsarlo, la aplicación debe tirar todos los ataques de todas las criaturas individuales de ese grupo.

No es necesario incluir ataque individual por criatura en el MVP.

La aplicación debe mostrar claramente qué resultado corresponde a cada criatura y a cada ataque.

Ejemplo:

```text
Badger 1
Bite: ataque 20 | daño 6 piercing
Claw 1: ataque 14 | daño 5 slashing
Claw 2: ataque 25 | daño 6 slashing

Badger 2
Bite: ataque 21 | daño 5 piercing
Claw 1: ataque 12 | daño 5 slashing
Claw 2: ataque 18 | daño 6 slashing
```

La aplicación debe tirar ataque y daño siempre, aunque luego el usuario determine en mesa que el ataque falla.

## 11. Críticos

La aplicación debe gestionar críticos de forma automática.

Cuando un ataque amenace crítico, debe mostrar:

- tirada de amenaza;
- tirada de confirmación;
- daño normal;
- daño crítico.

El daño normal debe mostrarse siempre, incluso cuando haya crítico.

Ejemplo:

```text
Badger 1
Bite
Amenaza: 20
Confirmación: 18
Daño normal: 6 piercing
Daño crítico: 12 piercing
```

Si el ataque tiene daño adicional elemental o similar marcado como no multiplicable en crítico, ese componente debe seguir apareciendo una vez en el daño crítico, sin duplicarse.

Ejemplo:

```text
Daño normal: 6 piercing + 1 fire
Daño crítico: 12 piercing + 1 fire
```

## 12. Tipos de daño

Cuando un ataque tenga varios tipos de daño o daño adicional, la aplicación debe mostrar el daño separado.

Ejemplo:

```text
Bite: 6 piercing + 1 acid
```

No debe mostrar únicamente el daño total.

## 13. Salvaciones de todas las criaturas de un tipo

Cada grupo de criaturas debe tener un botón:

```text
Salvaciones
```

Al pulsarlo, la aplicación debe tirar las tres tiradas de salvación para cada criatura individual del grupo:

- Fortaleza;
- Reflejos;
- Voluntad.

Ejemplo:

```text
Badger 1: Fort 18 / Ref 12 / Will 9
Badger 2: Fort 21 / Ref 15 / Will 4
Badger 3: Fort 11 / Ref 19 / Will 13
```

No es necesario incluir botones separados para Fortaleza, Reflejos y Voluntad en el MVP.

La cabecera de la pantalla principal debe incluir además los botones globales:

```text
Atacar con todas
Tirar TS con todas
```

## 14. Expandir ficha

Cada grupo de criatura debe tener un botón:

```text
Expandir ficha
```

Al pulsarlo, debe mostrarse la ficha completa de la criatura final.

La ficha expandida debe ser la ficha ya transformada:

- con `Augment Summoning` aplicado;
- con plantilla aplicada, si procede;
- con `Deep Guardian` aplicado, si procede.

No es necesario poder ver la ficha base sin transformar.

La ficha expandida puede incluir habilidades especiales, estadísticas completas, texto descriptivo y cualquier otro dato presente en el JSON final o derivado.

Si se usa un campo técnico para el texto completo de ficha, debe llamarse `fullStatBlock`. En la ficha expandida debe corresponder a la criatura final transformada, no a la ficha base sin modificar.

## 15. Persistencia del estado

La aplicación debe persistir el estado actual en todo momento.

Debe conservar, como mínimo:

- invocaciones activas;
- grupos activos;
- criaturas individuales;
- PG actuales;
- usos diarios máximos;
- usos diarios restantes;
- configuración de nivel máximo de `Summon Monster`;
- últimas criaturas usadas;
- criaturas más usadas.

Si se recarga la página, se cierra el navegador o se bloquea la tablet, el estado debe recuperarse.

## 16. Contador de usos diarios

La pantalla principal debe mostrar un contador de usos diarios.

Debe permitir:

- configurar usos máximos;
- ver usos restantes;
- sumar usos manualmente;
- restar usos manualmente;
- resetear usos diarios;
- descontar 1 uso automáticamente al invocar.

Reglas de límites:

- `maximum >= 0`.
- `0 <= remaining <= maximum`.
- Sumar usos manualmente aumenta `remaining` hasta un máximo de `maximum`.
- Restar usos manualmente reduce `remaining` hasta un mínimo de 0.
- Resetear usos diarios deja `remaining = maximum`.
- Invocar con `remaining > 0` resta 1 uso.
- Invocar con `remaining = 0` no bloquea rígidamente en el MVP, pero mantiene `remaining = 0` y no persiste valores negativos.
- Si se reduce `maximum` por configuración y queda por debajo de `remaining`, la aplicación ajusta `remaining = min(remaining, maximum)`.

## 17. Resultado de tiradas

La aplicación debe mostrar el resultado de la tirada en una modal o panel temporal.

No es necesario mantener un bloque persistente de "último resultado" en la pantalla principal.

Si el usuario cierra la modal y necesita la misma tirada otra vez, puede repetir la acción correspondiente.

## 18. Modo oscuro

La aplicación debe tener modo oscuro desde el inicio.

El diseño visual debe priorizar:

- legibilidad;
- contraste;
- botones grandes;
- pocos pasos;
- uso cómodo en tablet.

---

# Fase 2 — Mejoras de usabilidad

La Fase 2 añade comodidad y velocidad, pero no debe cambiar la filosofía del producto.

La aplicación seguirá sin intentar automatizar todas las reglas de Pathfinder.

## 1. Favoritos

Permitir marcar combinaciones de criatura + plantilla como favoritas.

Los favoritos deben servir para invocar opciones frecuentes con menos pasos.

Ejemplo:

```text
Fiery Badger
Chthonic Earth Elemental
Celestial Eagle
```

## 2. Duplicar una invocación previa

Permitir repetir rápidamente una invocación ya realizada.

La duplicación debe reutilizar:

- criatura base;
- plantilla;
- nivel de `Summon Monster` usado;
- reglas fijas aplicables.

La nueva invocación debe volver a tirar el número de criaturas si corresponde.

También debe descontar 1 uso diario.

## 3. Filtros por nivel de Summon Monster

En el modal de invocación, permitir filtrar criaturas por nivel de `Summon Monster`.

Ejemplo:

- `Summon Monster I`
- `Summon Monster II`
- `Summon Monster III`

El filtro debe ayudar a encontrar criaturas, no sustituir la configuración global de nivel máximo.

## 4. Botones rápidos por rol táctico

Se podrán añadir accesos rápidos por rol táctico si se consideran útiles tras probar el MVP en mesa.

Ejemplos posibles:

- aguantar;
- daño;
- volar;
- bloquear paso;
- movilidad;
- utilidad.

Estos botones no deben convertirse en una lista excesiva de opciones.

La prioridad seguirá siendo no saturar la interfaz.

Si el diagrama de elección se implementa en una fase futura, estos botones pueden apoyarse en ese mismo criterio de recomendación.

## 5. Registro simple de tiradas recientes

Añadir un registro corto de tiradas recientes.

Debe ser simple y no convertirse en un historial complejo.

Puede incluir, por ejemplo:

- últimas tiradas de ataque;
- últimas tiradas de TS;
- última tirada de número de criaturas invocadas.

Debe poder limpiarse fácilmente.

## 6. Mejoras sobre últimas y más usadas

Mejorar las listas de:

- últimas usadas;
- más usadas.

Posibles mejoras:

- mostrar fecha o uso reciente;
- priorizar por frecuencia;
- permitir eliminar entradas;
- distinguir claramente criatura base y plantilla;
- mantener un `shortcutId` estable para que la API pueda invocar desde esa entrada sin ambigüedad.

---

# Fuera de alcance inicial

Queda fuera del alcance inicial:

- control exacto de duración en asaltos;
- gestión completa de estados;
- automatización completa de buffs/debuffs;
- gestión de modificadores temporales;
- cálculo automático de carga, flanqueo, inspiración de bardo u otros modificadores situacionales;
- integración con Fantasy Grounds;
- multijugador;
- login;
- seguridad;
- gestión de usuarios;
- gestión de iniciativa;
- gestión de enemigos;
- gestión completa del personaje;
- mapa táctico;
- posicionamiento;
- medición de distancias;
- automatización de ataques especiales contextuales;
- gestión compleja de hechizos de criaturas invocadas;
- gestión de auras;
- validación legal completa de las criaturas disponibles;
- base de datos completa de Pathfinder;
- importación automática desde fuentes externas;
- explicación completa de reglas de Pathfinder.

---

# Criterios de aceptación de la Fase 1

La Fase 1 se considerará cumplida cuando sea posible usar la aplicación durante una partida real para:

1. Abrir la pantalla principal de combate.
2. Ver los usos diarios restantes.
3. Invocar una criatura disponible desde JSON.
4. Aplicar automáticamente reglas fijas del personaje.
5. Generar automáticamente el número de criaturas invocadas.
6. Ver el grupo de criaturas activas en pantalla.
7. Ver cards individuales con PG actuales y máximos.
8. Ajustar PG de una criatura individual.
9. Ajustar PG de una criatura individual.
10. Eliminar una criatura individual.
11. Limpiar todas las invocaciones activas.
12. Tirar todos los ataques de todas las criaturas de un grupo.
13. Ver daño normal y crítico cuando proceda.
14. Ver daños separados por tipo.
15. Tirar Salvaciones de todas las criaturas de un grupo.
16. Expandir la ficha completa final de una criatura.
17. Recargar la página y conservar el estado activo.

---

# Criterio práctico de éxito

El criterio práctico de éxito será la mesa.

La aplicación cumple su propósito si permite manejar las criaturas invocadas de forma rápida, sin tener que consultar constantemente un documento largo de referencia y sin ralentizar el combate.

