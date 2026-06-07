# Visión del producto

Aplicación web local para gestionar rápidamente en mesa las invocaciones de un **Inquisidor enano Neutral Bueno** con arquetipo **Monster Tactician** en **Pathfinder 1e**.

La aplicación está pensada como una herramienta personal de apoyo en combate, usada desde una **tablet Android**, para reducir el tiempo de consulta, gestión de PG y tiradas cuando hay varias criaturas invocadas activas.

No pretende sustituir al personaje, al máster, a los dados físicos ni a una herramienta de mesa virtual. Su objetivo es resolver de forma rápida y fiable las operaciones repetitivas asociadas a las invocaciones.

---

# Objetivo principal

Reducir el tiempo de consulta y gestión durante el combate, especialmente cuando hay varias criaturas invocadas activas.

La aplicación debe permitir:

- invocar criaturas rápidamente;
- generar el número de criaturas convocadas;
- aplicar las reglas fijas del personaje;
- mostrar las criaturas activas agrupadas por tipo;
- gestionar PG actuales de cada criatura individual;
- tirar ataques y daños de todas las criaturas iguales;
- tirar las tres tiradas de salvación de todas las criaturas iguales;
- consultar la ficha completa expandida cuando sea necesario.

El criterio de éxito principal será práctico:

> La aplicación debe permitir manejar las invocaciones en mesa de forma suficientemente rápida como para no necesitar imprimir ni consultar constantemente un documento largo de referencia.

---

# Contexto de uso

La aplicación será usada por una única persona: el jugador/máster que controla al Inquisidor.

Se usará durante la partida, en mesa, normalmente junto a:

- ficha del personaje;
- papel;
- lápices;
- dados físicos;
- otros apuntes o documentos de apoyo.

No es necesario que pueda usarse cómodamente con una sola mano, pero sí debe estar optimizada para uso rápido en una tablet Android, principalmente en orientación vertical.

Debe poder usarse también en horizontal y desde el navegador de un ordenador, aunque esa no es la experiencia prioritaria.

---

# Principios de diseño

## Rapidez sobre completitud

La prioridad es reducir fricción durante el combate.

La aplicación no debe intentar automatizar todas las reglas de Pathfinder. Debe resolver bien las operaciones repetitivas y dejar al jugador las decisiones contextuales.

Ejemplo:

- La app puede tirar `1d20 + ataque`.
- La app no debe intentar calcular si hay carga, flanqueo, inspiración de bardo, buffs temporales, penalizadores situacionales, etc.

## No hacer mal las reglas

Aunque se priorice la sencillez, la aplicación no debe aplicar reglas incorrectas.

Cuando una regla sea demasiado contextual o compleja, es preferible no automatizarla y dejarla en manos del jugador.

## Todo lo frecuente debe estar a pocos toques

Las acciones más frecuentes durante el combate deben estar visibles o muy accesibles:

- invocar;
- dañar;
- curar;
- eliminar criatura;
- atacar con todas;
- tirar salvaciones;
- expandir ficha.

## Lo raro puede quedar fuera

No deben implementarse sistemas complejos para casos poco frecuentes si eso hace más lenta o confusa la herramienta.

---

# Alcance inicial

## Incluido en el MVP

El MVP debe permitir:

- cargar criaturas desde ficheros JSON mantenidos manualmente;
- definir un nivel máximo actual de `Summon Monster`;
- invocar criaturas disponibles para ese nivel o inferiores;
- calcular automáticamente el número de criaturas invocadas;
- aplicar las reglas fijas del personaje;
- persistir el estado actual de las invocaciones;
- mostrar las invocaciones activas en la pantalla principal;
- agrupar criaturas iguales con la misma plantilla;
- gestionar PG individuales;
- tirar ataques y daños de todas las criaturas del mismo tipo;
- tirar Fortaleza, Reflejos y Voluntad de todas las criaturas del mismo tipo;
- mostrar la ficha completa expandida de la criatura ya transformada.

## Fuera del MVP

Queda fuera del MVP:

- diagrama de preguntas para elegir la invocación;
- gestión completa del personaje;
- gestión de iniciativa;
- gestión de enemigos;
- integración con Fantasy Grounds;
- control de mapa o posicionamiento;
- gestión de usuarios;
- seguridad;
- reglas completas de Pathfinder;
- automatización de buffs y debuffs temporales;
- gestión de duración asalto a asalto;
- uso complejo de hechizos de criaturas invocadas;
- gestión de auras;
- gestión de ataques o reglas raras altamente contextuales;
- explicación completa de reglas salvo al expandir ficha;
- base de datos completa de Pathfinder.

---

# Relación con Fantasy Grounds

La aplicación no tendrá vínculo con Fantasy Grounds.

No debe integrarse con Fantasy Grounds ni asumir que Fantasy Grounds está presente.

La aplicación funcionará como herramienta local independiente y personal.

---

# Duración de invocaciones

La aplicación no controlará la duración asalto a asalto.

Las invocaciones del personaje duran minutos por nivel de personaje, por lo que en la práctica duran lo suficiente como para que el control de duración no sea relevante para esta herramienta.

Sí debe existir un botón general para limpiar todas las invocaciones activas.

---

# Usos diarios

La aplicación debe mostrar y gestionar un contador de usos diarios de la aptitud de invocación.

Debe incluir:

- usos máximos configurables;
- usos restantes;
- botón para sumar usos;
- botón para restar usos;
- botón para resetear usos diarios;
- descuento automático de 1 uso al invocar.

Reglas funcionales del contador:

- `maximum` debe ser un entero mayor o igual que 0.
- `remaining` debe ser un entero mayor o igual que 0.
- `remaining` nunca puede superar `maximum`.
- El estado persistido debe cumplir siempre `maximum >= 0` y `0 <= remaining <= maximum`.
- Al invocar, si `remaining > 0`, se descuenta 1 uso.
- Al invocar con `remaining = 0`, el MVP no debe bloquear rígidamente la invocación por flexibilidad de mesa, pero `remaining` debe quedarse en 0 y no bajar nunca a negativo.
- Sumar usos no puede dejar `remaining > maximum`.
- Restar usos no puede dejar `remaining < 0`.
- Resetear usos deja `remaining = maximum`.

---

# Reglas fijas del personaje

La aplicación estará acoplada a este personaje concreto.

Debe tener en cuenta las siguientes reglas fijas:

## Nivel máximo de Summon Monster

Debe existir una variable de configuración que indique el nivel máximo actual de `Summon Monster` disponible.

Ejemplo actual:

```text
maxSummonMonsterLevel = 3
````

La aplicación debe permitir invocar criaturas del nivel máximo o inferiores según los datos JSON disponibles.

## Número de criaturas invocadas

Cuando se invoque una criatura de un nivel inferior al máximo disponible, se debe tirar automáticamente el número de criaturas.

Regla:

* criatura de nivel máximo: normalmente 1 criatura;
* criatura de nivel máximo - 1: `1d3 + 1`;
* criatura de nivel máximo - 2: `1d4 + 2`.

El `+1` adicional procede de la regla fija del personaje.

La aplicación debe hacer esta tirada automáticamente al invocar.

## Augment Summoning

Toda invocación aplicará:

```text
+4 Fuerza
+4 Constitución
```

respecto a la ficha básica almacenada.

La ficha JSON base no tiene por qué venir ya aumentada. La aplicación debe aplicar esta transformación.

## Superior Summoning

Siempre que se lance un dado para determinar el número de criaturas invocadas, se suma 1 criatura adicional.

Por eso las cantidades relevantes son:

```text
1d3 + 1
1d4 + 2
```

## Versatile Summon Monster

Si la criatura lo permite, se deberá aplicar una de estas plantillas:

* `Chthonic`
* `Fiery`
* `Celestial`
* `Entropic`
* `Resolute`

La ficha almacenada no incluye estas plantillas aplicadas. La aplicación debe aplicar la plantilla sobre la ficha base.

Si la criatura se introduce manualmente al invocar, la aplicación debe preguntar qué plantilla aplicar.

Si la criatura se selecciona desde últimas usadas o más usadas, la aplicación debe invocarla mediante el identificador estable del acceso rápido (`shortcutId`), reutilizando la combinación de criatura base + plantilla ya guardada en ese shortcut.

## Deep Guardian

Toda invocación que, tras aplicar plantilla o no aplicarla si no procede, cumpla una de estas condiciones:

* tenga velocidad de tipo `burrow`;
* sea del subtipo `earth`;

recibirá:

```text
+1 al ataque
+1 a la CA normal y a la CA desprevenida
```

No modifica la CA de toque.

---

# Datos de criaturas

Las criaturas se cargarán desde JSON añadidos manualmente.

La aplicación debe asumir que los datos introducidos son correctos.

No es responsabilidad de la aplicación validar si una criatura es legal, si tiene erratas o si la interpretación de reglas es correcta.

La aplicación sí debe aplicar las transformaciones fijas del personaje sobre esos datos.

---

# Ficha resumida visible

Cada tipo de criatura activa debe mostrar, sin necesidad de expandir, una ficha resumida con:

* nombre final de la criatura;
* alineamiento, tamaño y tipo;
* iniciativa;
* sentidos;
* percepción;
* CA;
* CA de toque;
* CA desprevenida;
* PG máximos;
* Fortaleza;
* Reflejos;
* Voluntad;
* velocidades;
* ataques;
* daño de los ataques;
* espacio;
* alcance;
* ataques especiales.

Ejemplo de información mínima visible:

```text
Badger (final sin plantilla)
N Small animal
Init +1; Senses low-light vision, scent; Perception +5

AC 14, touch 12, flat-footed 13 (+1 Dex, +1 natural, +1 size, +1 Deep Guardian a CA normal y desprevenida)
HP 8 (1d8+4)
Fort +6, Ref +3, Will +1

Speed 30 ft., burrow 10 ft.
Melee bite +6 (1d3+4), 2 claws +6 (1d2+4)
Space 5 ft., Reach 5 ft.
Special Attacks blood rage
```

No deben mostrarse notas tácticas en la ficha resumida.

---

# Ficha expandida

Cada bloque de criatura debe tener un botón:

```text
Expandir ficha
```

Al pulsarlo, se abrirá un modal o panel con la ficha completa de la criatura final.

La ficha expandida debe mostrar la versión ya transformada:

* con Augment Summoning aplicado;
* con plantilla aplicada, si procede;
* con Deep Guardian aplicado, si procede.

No es necesario poder ver la ficha base sin transformar.

El nombre técnico canónico del texto completo de ficha es `fullStatBlock`: en la criatura base representa la ficha original almacenada y en la criatura final representa la ficha transformada disponible para mesa.

---

# Pantalla principal

La pantalla principal será el centro de uso de la aplicación.

Debe mostrar:

* botón `Invocar`;
* botón `Limpiar invocaciones`;
* contador de usos diarios;
* lista de bloques de criaturas activas.

Las criaturas activas se agrupan por:

* criatura base;
* plantilla aplicada;
* resultado final de reglas fijas.

Si se invoca dos veces la misma criatura con la misma plantilla, se agrupan en el mismo bloque.

Ejemplo:

```text
Primer conjuro: 3 Fiery Badger
Segundo conjuro: 2 Fiery Badger

Resultado: un bloque Fiery Badger con 5 cards individuales.
```

Si es la misma criatura con distinta plantilla, deben ser bloques separados.

Ejemplo:

```text
Celestial Badger
Fiery Badger
Chthonic Badger
```

---

# Bloque de tipo de criatura

Cada bloque de criatura activa representa un tipo concreto de criatura final.

Debe incluir:

* ficha resumida;
* botón `Atacar con todas`;
* botón `Tirar TS`;
* botón `Expandir ficha`;
* lista de cards individuales de criaturas activas de ese tipo.

---

# Cards individuales de criatura

Cada criatura individual debe mostrarse como una card pequeña.

Cada card debe mostrar:

* número o identificador de criatura dentro del grupo;
* PG actuales;
* PG máximos;
* botón `Dañar`;
* botón `Curar`;
* botón `Eliminar`.

El botón `Eliminar` debe estar visible siempre.

Si una criatura queda a 0 PG o menos, la interfaz debe hacerlo evidente, pero no debe eliminarla automáticamente.

Algunas criaturas pueden seguir operando por debajo de 0 PG, por lo que deben poder seguir siendo dañadas, curadas o eliminadas manualmente.

---

# Gestión de daño y curación

El daño y la curación se aplican individualmente a cada criatura.

Al pulsar `Dañar` o `Curar`, la aplicación debe permitir:

* usar botones rápidos;
* introducir una cantidad entera positiva mayor o igual que 1 manualmente.

Ejemplo de botones rápidos:

```text
-1
-5
-10
+1
+5
+10
```

Estos botones son etiquetas de interfaz. El payload enviado a la API debe usar siempre un `amount` positivo: por ejemplo, el botón `-5` de daño envía `{ "amount": 5 }` al endpoint de daño y el botón `+5` de curación envía `{ "amount": 5 }` al endpoint de curación.

También debe existir un campo numérico para introducir una cantidad entera positiva mayor o igual que 1.

Si la criatura tiene:

* reducción de daño;
* resistencia a algún tipo de daño;
* inmunidad a algún tipo de daño;

esa información debe aparecer como nota en el modal de daño, para que el jugador la tenga en cuenta antes de introducir el valor final.

La aplicación no debe calcular automáticamente reducciones, resistencias ni inmunidades al aplicar daño.

---

# Flujo de invocación

Al pulsar `Invocar`, la aplicación debe abrir un modal de selección.

El flujo esperado es:

1. Abrir modal de invocación.
2. Elegir criatura mediante:

   * últimas usadas;
   * más usadas;
   * búsqueda manual.
3. Si la selección es por búsqueda manual y requiere elegir plantilla, preguntar plantilla.
4. Si la selección es desde últimas usadas o más usadas, enviar el `shortcutId` de la opción elegida y no volver a preguntar plantilla.
5. Calcular automáticamente el número de criaturas invocadas.
6. Descontar automáticamente 1 uso diario si quedan usos disponibles; si `remaining = 0`, permitir la invocación sin bajar el contador a negativo.
7. Añadir las criaturas activas a la pantalla principal.
8. Persistir inmediatamente el nuevo estado.

El diagrama de preguntas para elegir la invocación queda fuera del MVP.

---

# Últimas usadas y más usadas

La aplicación puede ofrecer accesos rápidos a:

* últimas criaturas invocadas;
* criaturas más usadas.

Cada entrada de estas listas representa una combinación concreta de criatura base + plantilla y debe tener un identificador estable (`shortcutId`).

Cuando se seleccione una entrada desde estas listas, el frontend debe enviar ese `shortcutId` a la API. La API resolverá desde el shortcut la criatura base y la plantilla guardadas.

Esto permite invocar con menos pasos las opciones frecuentes sin que el frontend tenga que reconstruir lógica de plantillas.

---

# Tiradas de ataque

Cada bloque de criatura tendrá un botón:

```text
Atacar con todas
```

Este botón debe tirar todos los ataques de todas las criaturas individuales de ese tipo.

No es necesario atacar con una criatura individual desde la aplicación.

La aplicación debe mostrar claramente a qué criatura corresponde cada tirada.

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

La aplicación debe tirar ataque y daño siempre, aunque luego el jugador determine que el ataque falla.

Los modificadores temporales o situacionales se gestionan manualmente fuera de la aplicación.

---

# Críticos

La aplicación debe gestionar críticos de forma automática.

Cuando un ataque amenace crítico, debe mostrar:

* tirada de amenaza;
* tirada de confirmación;
* daño normal;
* daño crítico.

También debe mostrarse el daño normal aunque haya crítico, por si el jugador decide usarlo o necesita corregir algo manualmente.

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

---

# Tipos de daño

Cuando un ataque tenga varios tipos de daño o daño adicional, la aplicación debe mostrar el daño separado.

Ejemplo:

```text
Bite: 6 piercing + 1 acid
```

No debe limitarse a mostrar solo el daño total.

---

# Tiradas de salvación

Cada bloque de criatura tendrá un botón:

```text
Tirar TS
```

Este botón debe tirar las tres salvaciones para todas las criaturas individuales de ese tipo:

* Fortaleza;
* Reflejos;
* Voluntad.

Ejemplo:

```text
Badger 1: Fort 18 / Ref 12 / Will 9
Badger 2: Fort 21 / Ref 15 / Will 4
Badger 3: Fort 11 / Ref 19 / Will 13
```

No es necesario tener botones separados para Fortaleza, Reflejos o Voluntad.

---

# Persistencia

El estado actual debe quedar persistido en todo momento.

Si se recarga la página, se cierra el navegador o se bloquea la tablet, la aplicación debe poder recuperar:

* invocaciones activas;
* criaturas individuales;
* PG actuales;
* usos diarios restantes;
* configuración relevante.

No es necesario guardar historial de tiradas.

El resultado actual o más reciente de una tirada puede mostrarse en pantalla. Debe poder sustituirse al realizar otra tirada y debe poder limpiarse manualmente sin limpiar las invocaciones activas.

Si se necesita una tirada otra vez, el jugador puede repetir la acción correspondiente.

---

# Interfaz

La interfaz debe priorizar rapidez de uso.

La estética es secundaria frente a:

* claridad;
* botones grandes;
* pocos pasos;
* información visible;
* ausencia de navegación innecesaria.

La estructura preferida es:

* una página principal;
* modales o paneles para acciones secundarias.

Ejemplos de modales:

* invocar criatura;
* dañar criatura;
* curar criatura;
* expandir ficha;
* ver resultado de tirada;
* limpiar el resultado de tirada actual sin borrar invocaciones.

La aplicación debe tener modo oscuro desde el inicio.

---

# No objetivos iniciales

La aplicación no debe:

* gestionar todo el personaje;
* sustituir una herramienta de mesa virtual;
* integrarse con Fantasy Grounds;
* automatizar todos los efectos de Pathfinder;
* controlar duración de invocaciones asalto a asalto;
* explicar reglas completas salvo al expandir ficha;
* incluir seguridad;
* incluir usuarios múltiples;
* gestionar iniciativa;
* gestionar enemigos;
* gestionar personajes jugadores;
* resolver posicionamiento en mapa;
* aplicar automáticamente buffs o debuffs complejos;
* gestionar modificadores temporales;
* resolver reglas raras o contextuales de ataques especiales;
* gestionar uso complejo de hechizos de criaturas invocadas;
* gestionar auras;
* sustituir el criterio del jugador o del máster;
* ser una base de datos completa de Pathfinder.

---

# Decisiones tomadas

* La aplicación será de uso personal.
* Se usará principalmente desde tablet Android en vertical.
* Será una aplicación web local.
* No habrá seguridad ni usuarios múltiples.
* No habrá integración con Fantasy Grounds.
* El estado debe persistirse automáticamente.
* Las criaturas se cargarán desde JSON mantenidos manualmente.
* La aplicación asumirá que los datos JSON son correctos.
* La ficha base no tendrá plantillas ni mejoras aplicadas.
* La aplicación aplicará las reglas fijas del personaje.
* El MVP cubrirá las criaturas añadidas en JSON y disponibles según el nivel máximo configurado de `Summon Monster`.
* El nivel máximo actual será configurable.
* Actualmente el personaje usa `Summon Monster III` o inferior.
* La duración de las invocaciones no se controlará asalto a asalto.
* Sí habrá botón para limpiar todas las invocaciones activas.
* Sí habrá contador de usos diarios.
* Invocar descontará automáticamente un uso diario.
* Las criaturas iguales con la misma plantilla se agruparán.
* Las criaturas iguales con distinta plantilla se mostrarán en bloques distintos.
* Cada criatura individual tendrá su propia card de PG.
* El daño y la curación se aplicarán individualmente.
* La app tirará ataques y daños.
* La app tirará todos los ataques de todas las criaturas del mismo tipo.
* La app no gestionará ataques individuales.
* La app tirará las tres TS de todas las criaturas del mismo tipo.
* No habrá botones separados para cada TS.
* La app gestionará críticos mostrando amenaza, confirmación, daño normal y daño crítico.
* Los tipos de daño se mostrarán separados.
* No habrá modificadores temporales.
* No habrá notas tácticas en la ficha resumida.
* El botón será `Expandir ficha`, no `Expandir habilidades`.
* La ficha expandida mostrará toda la ficha final transformada.
* No es necesario ver la ficha base sin transformar.
* El diagrama de elección de invocación queda fuera del MVP.