# Glosario

Este documento define los términos principales usados en el proyecto.

Su objetivo es evitar ambigüedades durante el desarrollo, especialmente cuando intervengan herramientas de IA o asistentes de código.

---

# Sistema de juego

## Pathfinder 1e

Abreviatura de **Pathfinder 1ª edición**.

Este proyecto usa reglas, terminología y estructura de Pathfinder 1e.

No deben usarse reglas de:

- Pathfinder 2e;
- D&D 5e;
- Starfinder;
- otros sistemas d20 derivados.

Cuando haya duda de reglas, nombres de estadísticas o funcionamiento de una mecánica, debe asumirse Pathfinder 1e.

## Pathfinder 2e

Pathfinder 2ª edición.

Está fuera del alcance del proyecto.

No deben importarse conceptos como:

- acciones de tres puntos;
- proficiency por grados;
- condiciones o etiquetas propias de PF2e;
- estructura de monstruos de PF2e;
- reglas de invocación de PF2e.

## D&D 5e

Dungeons & Dragons 5ª edición.

Está fuera del alcance del proyecto.

No deben usarse conceptos como:

- ventaja/desventaja;
- bonus action;
- bounded accuracy;
- proficiency bonus;
- concentration como en 5e;
- formato de monstruos de 5e.

## Starfinder

Sistema de ciencia ficción derivado de Pathfinder.

Está fuera del alcance del proyecto.

No deben usarse reglas, estadísticas ni términos de Starfinder.

---

# Personaje del proyecto

## Inquisidor

Clase del personaje del usuario en Pathfinder 1e.

En este proyecto, el Inquisidor es el personaje que genera y controla las invocaciones.

La aplicación no gestiona todo el Inquisidor. Solo gestiona las invocaciones relevantes para mesa.

## Monster Tactician

Arquetipo de Inquisidor especializado en invocaciones.

En este proyecto es relevante porque:

- el personaje usa una aptitud de invocación basada en `Summon Monster`;
- las invocaciones duran minutos por nivel;
- la aplicación debe gestionar usos diarios;
- la aplicación está acoplada a reglas fijas concretas del personaje.

## Inquisidor enano Neutral Bueno

Descripción concreta del personaje del usuario.

El proyecto está acoplado a este personaje, no pensado inicialmente como herramienta genérica universal.

La aplicación debe tener en cuenta sus reglas fijas, dotes y opciones de invocación.

## NB

Neutral Bueno.

Alineamiento del personaje del usuario.

En inglés puede aparecer como `NG`.

## Enano

Raza del personaje del usuario.

En el MVP no implica una funcionalidad concreta de la aplicación, pero forma parte del contexto del producto.

---

# Invocaciones

## Summon Monster

Línea de conjuros de invocación usada como base para las criaturas.

En este proyecto, `Summon Monster` se usa como referencia para:

- determinar qué criaturas puede invocar el personaje;
- clasificar criaturas por nivel;
- calcular cuántas criaturas aparecen cuando se invoca una criatura de nivel inferior.

## Nivel de Summon Monster

Nivel de la lista de `Summon Monster` al que pertenece una criatura.

Ejemplos:

- `Summon Monster I`;
- `Summon Monster II`;
- `Summon Monster III`.

En el modelo de datos se representa como:

```text
nivelSummon
```

## Nivel máximo de Summon Monster

Nivel máximo actual de `Summon Monster` que puede usar el personaje.

En configuración puede representarse como:

```text
maxSummonMonsterLevel
```

Ejemplo inicial:

```text
maxSummonMonsterLevel = 3
```

Este valor permite saber:

- qué criaturas están disponibles;
- si una invocación genera 1 criatura;
- si debe tirarse `1d3 + 1`;
- si debe tirarse `1d4 + 2`.

## Criatura invocable

Criatura que está disponible en el catálogo JSON y puede ser seleccionada para invocar.

La aplicación no valida si una criatura es legal o no. Asume que las criaturas introducidas en JSON son correctas.

## Criatura base

Ficha original almacenada en el catálogo JSON antes de aplicar reglas fijas del personaje.

La criatura base puede no incluir:

- `Augment Summoning`;
- plantilla de `Versatile Summon Monster`;
- mejora de `Deep Guardian`.

## Criatura final

Versión resultante tras aplicar a la criatura base las reglas fijas correspondientes.

La criatura final es la que se usa en mesa.

Debe ser la que aparece en:

- pantalla principal;
- ficha resumida;
- ficha expandida;
- tiradas de ataque;
- tiradas de daño;
- tiradas de salvación;
- cards individuales.

## Tipo de criatura activa

Grupo de criaturas activas que comparten la misma criatura final.

Ejemplo:

```text
Fiery Badger x5
```

El grupo se forma por coincidencia de:

- criatura base;
- plantilla;
- reglas fijas aplicadas;
- ficha final resultante.

## Instancia de criatura

Una criatura concreta dentro de un grupo.

Ejemplo:

```text
Fiery Badger 1
Fiery Badger 2
Fiery Badger 3
```

Todas comparten la ficha de tipo, pero cada una tiene PG actuales independientes.

## Grupo

Bloque de la pantalla principal que representa un tipo de criatura activa.

Incluye:

- cabecera;
- ficha resumida;
- botones globales;
- cards individuales.

## Card

Representación visual de una criatura individual.

Debe mostrar:

- nombre visible de la criatura;
- PG máximos;
- PG actuales;
- botones rápidos de ajuste de PG;
- botón `Eliminar`.

---

# Reglas fijas del personaje

## Augment Summoning

Dote o regla fija del personaje que mejora las criaturas invocadas.

En este proyecto implica aplicar:

```text
+4 Fuerza
+4 Constitución
```

a toda criatura invocada.

La aplicación debe recalcular los valores funcionales afectados que se usen en mesa.

## Superior Summoning

Dote o regla fija que aumenta el número de criaturas invocadas cuando se tira cantidad.

En este proyecto ya está reflejada funcionalmente como:

```text
1d3 + 1
1d4 + 2
```

cuando se invocan criaturas de niveles inferiores al máximo disponible.

## Versatile Summon Monster

Opción que permite aplicar plantillas a criaturas invocadas si la criatura lo permite.

Plantillas previstas:

- `Chthonic`;
- `Fiery`;
- `Celestial`;
- `Entropic`;
- `Resolute`.

La aplicación debe preguntar plantilla cuando se selecciona una criatura manualmente y tiene varias plantillas disponibles.

Si se usa una opción de últimas usadas o más usadas, puede reutilizar directamente la combinación criatura + plantilla.

## Deep Guardian

Rasgo relevante del personaje.

En este proyecto implica:

```text
+1 al ataque
+1 a la CA
```

para toda criatura final que, tras aplicar plantilla o no aplicarla si no procede:

- tenga velocidad `burrow`; o
- tenga subtipo `earth`.

## Plantilla

Modificación aplicada sobre una criatura base para generar una criatura final.

Ejemplos:

- `Fiery Badger`;
- `Celestial Badger`;
- `Chthonic Badger`.

La plantilla puede modificar:

- alineamiento;
- subtipo;
- ataques;
- daño adicional;
- resistencias;
- inmunidades;
- habilidades;
- ficha expandida.

---

# Estadísticas básicas

## CA

Clase de Armadura.

En inglés, `AC`.

La aplicación debe mostrar:

- CA normal;
- CA de toque;
- CA desprevenida.

## CA de toque

Clase de Armadura contra ataques de toque.

En inglés, `touch AC`.

## CA desprevenida

Clase de Armadura cuando la criatura está desprevenida.

En inglés, `flat-footed AC`.

## PG

Puntos de golpe.

En inglés, `HP`.

La aplicación distingue:

- PG máximos;
- PG actuales.

## PG máximos

Cantidad máxima normal de PG de la criatura final.

Cada instancia de criatura del mismo grupo comparte los mismos PG máximos.

## PG actuales

PG concretos de una instancia individual.

Cada criatura individual tiene sus propios PG actuales.


## Cantidad de daño o curación

Valor introducido por el usuario para aplicar daño o curación a una instancia.

En API se representa como:

```text
amount
```

Debe ser siempre un entero positivo mayor o igual que 1.

No son válidos:

- `0`;
- números negativos;
- decimales;
- texto;
- `null`;
- valor vacío;
- ausencia del campo.

El signo no se envía en `amount`: el endpoint de daño resta ese valor y el endpoint de curación lo suma.


## Usos diarios máximos

Valor máximo de usos diarios de la aptitud de invocación.

En API y dominio se representa como:

```text
DailyUses.maximum
```

Debe ser siempre un entero mayor o igual que 0.

## Usos diarios restantes

Valor actual de usos diarios disponibles durante la sesión.

En API y dominio se representa como:

```text
DailyUses.remaining
```

Debe ser siempre un entero mayor o igual que 0 y nunca puede superar `DailyUses.maximum`.

Regla canónica:

```text
maximum >= 0
0 <= remaining <= maximum
```

Invocar con `remaining = 0` no bloquea rígidamente en el MVP, pero tampoco puede reducir los usos por debajo de 0.

## Resetear usos diarios

Acción manual que devuelve el contador de usos diarios a su valor máximo.

Resultado:

```text
remaining = maximum
```

## Iniciativa

Bonificador de iniciativa de la criatura.

Puede aparecer en fichas como:

```text
Init +1
```

## Velocidad

Movimiento de la criatura.

Puede incluir varios tipos:

- `land`;
- `fly`;
- `swim`;
- `climb`;
- `burrow`.

La velocidad `burrow` es relevante para `Deep Guardian`.

## Percepción

Bonificador de la habilidad Perception.

En la aplicación se muestra como dato de ficha, pero no se automatiza en el MVP.

## Sentidos

Capacidades sensoriales de la criatura.

Ejemplos:

- `low-light vision`;
- `darkvision`;
- `scent`;
- `tremorsense`.

## Espacio

Casillas o tamaño ocupado por la criatura en combate.

En inglés, `Space`.

Ejemplo:

```text
Space 5 ft.
```

## Alcance

Distancia a la que la criatura amenaza o puede atacar cuerpo a cuerpo.

En inglés, `Reach`.

Ejemplo:

```text
Reach 5 ft.
```

---

# Salvaciones

## TS

Tirada de salvación.

En inglés, `saving throw`.

En el MVP, hay un único botón:

```text
Salvaciones
```

Ese botón tira las tres salvaciones para todas las criaturas de un grupo.

## Fort

Fortaleza.

En inglés, `Fortitude`.

Salvación asociada normalmente a resistencia física, venenos, enfermedades y efectos corporales.

## Ref

Reflejos.

En inglés, `Reflex`.

Salvación asociada normalmente a esquivar explosiones, áreas y efectos similares.

## Vol

Voluntad.

En inglés, `Will`.

Salvación asociada normalmente a mente, compulsiones, ilusiones y efectos mentales.

## Fortaleza

Nombre completo en español de `Fort`.

## Reflejos

Nombre completo en español de `Ref`.

## Voluntad

Nombre completo en español de `Vol`.

---

# Ataques y daño

## Ataque

Acción ofensiva definida en la ficha de una criatura.

Ejemplos:

- mordisco;
- garra;
- golpe;
- aguijón;
- embestida.

En el catálogo se modela como un elemento estructurado con bonificador, daño, crítico y cantidad.

## Bonificador de ataque

Número que se suma a la tirada de d20 para atacar.

Ejemplo:

```text
d20 14 + 5 = 19
```

En este ejemplo, `+5` es el bonificador de ataque.

## Tirada de ataque

Resultado de lanzar 1d20 y sumar el bonificador de ataque.

La aplicación muestra:

- dado;
- modificador;
- total.

Ejemplo:

```text
d20 14 + 5 = 19
```

## Atacar

Botón global de un grupo.

Tira todos los ataques de todas las criaturas individuales de ese grupo.

No decide si impactan y no aplica daño automáticamente.

## Daño

Resultado de una fórmula de daño.

Ejemplo:

```text
1d6+3
```

La aplicación tira el daño junto al ataque y lo muestra como:

```text
Daño si impacta
```

## Daño si impacta

Daño tirado por la aplicación, pero no aplicado automáticamente.

El usuario decide si el ataque impacta y si debe aplicarse daño.

## Tipo de daño

Categoría del daño.

Ejemplos:

- `piercing`;
- `slashing`;
- `bludgeoning`;
- `fire`;
- `acid`;
- `cold`;
- `electricity`.

La aplicación debe mostrar los daños separados por tipo.

## Daño adicional

Daño extra de un ataque, plantilla o habilidad.

Ejemplo:

```text
1d3+3 piercing + 1 fire
```

Debe modelarse como componente separado de daño.

## Crítico

Regla de ataque que puede multiplicar daño cuando una tirada natural cae dentro del rango de amenaza y luego se confirma.

La aplicación debe mostrar amenaza, confirmación, daño normal y daño crítico.

## Amenaza de crítico

Tirada natural de ataque dentro del rango de crítico.

Ejemplo:

- con `20/x2`, amenaza solo con 20 natural;
- con `19-20/x2`, amenaza con 19 o 20 natural.

## Confirmación de crítico

Segunda tirada de ataque que determina si el crítico se confirma.

La aplicación tira confirmación, pero no decide si confirma contra la CA enemiga.

## Daño normal

Daño del ataque sin aplicar multiplicador de crítico.

Debe mostrarse siempre, incluso si hay amenaza de crítico.

## Daño crítico

Daño calculado con el multiplicador de crítico.

Debe mostrarse cuando haya amenaza de crítico.

## Multiplicador de crítico

Valor por el que se multiplica el daño aplicable en crítico.

Ejemplos:

- `x2`;
- `x3`;
- `x4`.

## Rango de crítico

Valor mínimo natural del d20 que amenaza crítico.

Ejemplos:

- `20`: amenaza con 20 natural;
- `19`: amenaza con 19-20;
- `18`: amenaza con 18-20.

---

# Defensas y resistencias

## RD

Reducción de daño.

En inglés, `DR`.

Ejemplo:

```text
RD 5/good
```

La aplicación debe mostrarla como nota en el modal de daño si la criatura la tiene.

No debe aplicarla automáticamente.

## Resistencia

Reducción fija contra un tipo de energía o daño.

Ejemplo:

```text
Resistencia fire 10
```

La aplicación debe mostrarla como nota, pero no aplicarla automáticamente.

## Inmunidad

Daño, condición o efecto que no afecta a la criatura.

Ejemplo:

```text
Inmune poison
```

La aplicación debe mostrarla como nota, pero no aplicarla automáticamente.

## Vulnerabilidad

Debilidad ante un tipo de daño o efecto.

La aplicación puede mostrarla como nota defensiva si aparece en el catálogo, pero no debe automatizarla en el MVP.

## Defensas especiales

Lista de RD, resistencias, inmunidades, vulnerabilidades u otras defensas relevantes.

Se usan principalmente para mostrar avisos en el modal de daño.

---

# Estados visuales/API de instancia

La instancia de criatura solo puede tener tres estados visibles/API en el MVP.

## Sana

Estado técnico: `HEALTHY`.

Criatura con PG actuales igual a PG máximos.

## Dañada

Estado técnico: `DAMAGED`.

Criatura con PG actuales mayor que 0 y menor que PG máximos.

## Caída

Estado técnico: `DOWN`.

Criatura con PG actuales igual o menor que 0.

La aplicación debe marcarla visualmente como caída, pero no eliminarla automáticamente.

## Eliminada

No es un estado de instancia.

Criatura retirada manualmente por el usuario.

Una criatura eliminada desaparece de la pantalla principal. Si era la última instancia de su grupo, el grupo también desaparece.

---

# Interfaz

## Pantalla principal

Pantalla central de combate.

Debe mostrar:

- acciones globales;
- usos diarios;
- grupos de criaturas activas;
- ficha resumida de cada grupo;
- cards individuales;
- resultados de tiradas.

## Ficha resumida

Bloque de datos mecánicos visibles sin expandir.

Debe contener lo necesario para usar la criatura en combate sin consultar otro documento.

No debe incluir notas tácticas en el MVP.

## `fullStatBlock`

Nombre técnico canónico del texto completo de ficha.

Usos:

- `CreatureTemplate.fullStatBlock`: ficha base almacenada en el catálogo, sin reglas fijas obligatoriamente aplicadas.
- `ResolvedCreature.fullStatBlock`: ficha final transformada si se puede generar como texto.
- `ExpandedStatBlockResponse.fullStatBlock`: ficha expandida final devuelta por la API para un grupo activo.

No deben crearse alias técnicos como `fichaCompleta`, `statBlockText` o `expandedStatBlockText` para representar el mismo concepto.

## Ficha expandida

Ficha completa de la criatura final.

Se abre mediante el botón:

```text
Expandir ficha
```

Debe mostrar la criatura ya transformada, no la ficha base sin modificar.

## Expandir ficha

Botón que abre la ficha completa de la criatura final.

No debe llamarse `Expandir habilidades`, porque puede incluir toda la ficha, no solo habilidades.

## Modal

Ventana o panel temporal para realizar una acción secundaria.

Ejemplos:

- invocar;
- ajustar PG;
- expandir ficha;
- mostrar tirada.

## Modo oscuro

Tema visual oscuro obligatorio desde el inicio.

Debe mejorar la lectura en mesa y reducir fatiga visual.

---

# Persistencia

## Persistencia

Capacidad de guardar el estado actual de la aplicación.

Debe ocurrir tras cambios relevantes.

## Estado persistido

Datos que deben conservarse aunque se recargue la página o se bloquee la tablet.

Incluye:

- invocaciones activas;
- grupos;
- instancias individuales;
- PG actuales;
- usos diarios;
- configuración;
- últimas usadas;
- más usadas.

## Recargar

Acción de volver a cargar la página.

La aplicación debe recuperar el estado anterior.

## Limpiar invocaciones

Botón global que elimina todas las criaturas activas.

Debe pedir confirmación.

Debe conservar configuración, usos diarios, últimas usadas y más usadas.

---

# Catálogo y datos

## Catálogo de criaturas

Conjunto de JSON que describe las criaturas base invocables.

Es la fuente de datos para generar criaturas finales.

## JSON

Formato de datos usado para definir criaturas.

La aplicación asume que los JSON introducidos son correctos.

## id

Identificador único y estable de una criatura base o elemento de datos.

Debe ser apto para uso técnico.

Ejemplo:

```text
badger
small-earth-elemental
```

## nombre

Nombre visible para el usuario.

Ejemplo:

```text
Badger
Small Earth Elemental
```

## subtipos

Lista de subtipos de una criatura.

Ejemplo:

```json
["earth", "elemental"]
```

Es relevante para reglas como `Deep Guardian`.

## plantillasPermitidas

Lista de plantillas que una criatura puede recibir.

Ejemplo:

```json
["celestial", "fiery", "chthonic"]
```

## habilidadesResumidas

Recordatorios breves de habilidades.

Pueden usarse en ficha expandida o fases futuras.

## habilidadesCompletas

Texto completo o suficientemente detallado de habilidades.

Deben poder consultarse en la ficha expandida.

## notasTacticas

Notas de uso táctico.

El campo puede existir en el catálogo, pero no se muestra en la ficha resumida del MVP.

---

# Tiradas y dados

## d20

Dado de veinte caras.

Se usa para ataques y salvaciones.

## d3

Dado de tres resultados.

En la práctica puede simularse como `1d3`.

Se usa para cantidad de criaturas invocadas.

## d4

Dado de cuatro caras.

Se usa para cantidad de criaturas invocadas.

## Fórmula de dados

Texto que representa una tirada.

Ejemplos:

```text
1d20+5
1d6+3
1d3+1
1d4+2
```

## Resultado de tirada

Salida visible generada por la aplicación tras tirar dados.

Debe mostrar dado, modificador y total cuando corresponda.

Ejemplo:

```text
d20 14 + 5 = 19
```

## Resultado más reciente

Última tirada visible en pantalla.

En el MVP no es necesario un historial completo.

---

# Fuera del MVP

## Diagrama de elección

Sistema futuro para recomendar qué criatura invocar mediante preguntas.

Está fuera del MVP.

## Modificadores temporales

Bonificadores o penalizadores de situación.

Ejemplos:

- carga;
- flanqueo;
- cobertura;
- inspiración de bardo;
- bendiciones;
- buffs;
- debuffs.

La aplicación no los gestiona en el MVP.

## Estado completo

Sistema completo de condiciones y estados de Pathfinder.

Está fuera del MVP.

La aplicación solo marca visualmente una criatura como caída si sus PG son 0 o menos.

## Fantasy Grounds

Herramienta de mesa virtual.

Este proyecto no se integra con Fantasy Grounds.

## Login

Sistema de autenticación de usuarios.

Fuera del MVP.

## Multijugador

Uso por varios usuarios.

Fuera del MVP.

## Seguridad

Gestión de autenticación, autorización o permisos.

Fuera del MVP porque la aplicación es local y personal.

---

# Decisiones terminológicas

- Usar `PG`, no `HP`, en textos visibles en español.
- Usar `CA`, no `AC`, en textos visibles en español.
- Usar `TS` para tiradas de salvación.
- Usar `Fort`, `Ref` y `Vol` como abreviaturas compactas.
- Usar `Atacar` para el botón de grupo.
- Usar `Atacar con todas` para el botón global de ataques.
- Usar `Tirar TS con todas` para el botón global de salvaciones.
- Usar `Expandir ficha`, no `Expandir habilidades`.
- Usar `Atacar`, `Salvaciones` y `Eliminar` en las cards o grupos.
- Usar `Limpiar invocaciones` para borrar todos los grupos activos.
- Usar `criatura final` para la ficha ya transformada.
- Usar `criatura base` para la ficha original del JSON.
