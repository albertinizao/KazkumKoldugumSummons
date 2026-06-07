# Pantalla principal

La pantalla principal es el centro de uso de la aplicación durante el combate.

Debe estar optimizada para usarse de forma rápida desde una **tablet Android**, principalmente en orientación vertical, mientras el usuario está en mesa con ficha, dados físicos, papel y lápices.

La prioridad de esta pantalla es permitir consultar criaturas activas, gestionar PG y resolver tiradas frecuentes con el menor número posible de toques.

---

# Objetivo de la pantalla

La pantalla principal debe permitir:

- ver todas las invocaciones activas;
- distinguir rápidamente los tipos de criatura presentes;
- consultar la ficha resumida de cada tipo de criatura;
- ver cuántas criaturas concretas hay de cada tipo;
- gestionar PG individuales;
- dañar, curar o eliminar criaturas concretas;
- tirar ataques de todas las criaturas de un mismo tipo;
- tirar las tres TS de todas las criaturas de un mismo tipo;
- abrir la ficha completa final de una criatura cuando haga falta;
- limpiar todas las invocaciones activas al terminar un combate o escena.

---

# Estructura general

La pantalla se organiza por **tipos de criatura activa**.

Cada tipo de criatura aparece como un bloque independiente.

Ejemplo:

```text
Pantalla principal

[Invocar] [Limpiar invocaciones] [Usos diarios: 4 / 6]

Fiery Badger
- Ficha resumida del tipo de criatura.
- Botones globales del grupo.
- Cards de criaturas activas de ese tipo.

Small Earth Elemental
- Ficha resumida del tipo de criatura.
- Botones globales del grupo.
- Cards de criaturas activas de ese tipo.
```

Las criaturas se agrupan por su versión final invocable, no solo por su criatura base.

Por tanto, estas criaturas deben aparecer en bloques distintos:

```text
Fiery Badger
Celestial Badger
Chthonic Badger
```

Si se invoca dos veces la misma criatura con la misma plantilla, se añade al mismo bloque.

Ejemplo:

```text
Primera invocación: 3 Fiery Badger
Segunda invocación: 2 Fiery Badger

Resultado en pantalla: 1 bloque Fiery Badger con 5 cards individuales.
```

---

# Cabecera de pantalla

La parte superior de la pantalla debe mostrar siempre las acciones globales.

Debe incluir:

- botón `Invocar`;
- botón `Limpiar invocaciones`;
- contador de usos diarios;
- acceso a configuración básica si fuera necesario.

## Botón Invocar

El botón `Invocar` abre el modal de invocación.

Desde ese modal se podrá seleccionar una criatura mediante:

- últimas usadas;
- más usadas;
- búsqueda manual.

El diagrama de elección de invocación queda fuera del MVP.

## Botón Limpiar invocaciones

El botón `Limpiar invocaciones` elimina todas las criaturas activas.

Debe pedir confirmación antes de borrar.

Este botón está pensado para cerrar un combate, limpiar la mesa o corregir rápidamente el estado entre escenas.

## Contador de usos diarios

La pantalla principal debe mostrar el estado de usos diarios.

Ejemplo:

```text
Usos diarios: 4 / 6
```

Debe permitir:

- ver usos restantes;
- ver usos máximos;
- sumar usos manualmente;
- restar usos manualmente;
- resetear usos diarios;
- descontar automáticamente 1 uso al invocar.

Comportamiento visible:

- El contador debe mostrarse siempre como `remaining / maximum`.
- `maximum` no puede ser negativo.
- `remaining` no puede ser negativo ni superar `maximum`.
- Si el usuario suma usos cuando `remaining = maximum`, el contador permanece en `maximum`.
- Si el usuario resta usos cuando `remaining = 0`, el contador permanece en 0.
- Al resetear, el contador queda en `maximum / maximum`.
- Si se invoca con `remaining > 0`, el contador baja en 1.
- Si se invoca con `remaining = 0`, la invocación puede continuar en el MVP, pero el contador permanece en 0.

---

# Bloque de tipo de criatura

Cada bloque representa un tipo concreto de criatura final activa.

Debe contener:

1. cabecera del grupo;
2. ficha resumida;
3. botones globales del grupo;
4. cards individuales de criaturas activas.

Ejemplo:

```text
Fiery Badger x5

N Small animal
Init +1; Senses low-light vision, scent; Perception +5

AC 14, touch 12, flat-footed 13
HP 15
Fort +8, Ref +3, Will +1

Speed 30 ft., burrow 10 ft.
Melee bite +4 (1d3+3 + 1 fire), 2 claws +4 (1d2+3 + 1 fire)
Space 5 ft., Reach 5 ft.
Special Attacks blood rage

[Atacar con todas] [Tirar TS] [Expandir ficha]

[Fiery Badger 1] [Fiery Badger 2] [Fiery Badger 3] ...
```

---

# Cabecera del grupo

La cabecera del grupo debe mostrar:

- nombre final de la criatura;
- número de criaturas activas de ese tipo.

Ejemplo:

```text
Fiery Badger x5
```

El nombre debe corresponder a la criatura final tras aplicar reglas fijas, plantilla y mejoras correspondientes.

---

# Ficha resumida de tipo de criatura

Cada bloque debe mostrar una ficha resumida visible sin necesidad de abrir la ficha completa.

La ficha resumida debe mostrar la información necesaria para usar la criatura en combate sin consultar otro documento.

Debe incluir:

- nombre final;
- alineamiento;
- tamaño;
- tipo;
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
- ataques especiales.

No debe incluir notas tácticas breves en el MVP.

La pantalla principal debe mostrar datos mecánicos, no recomendaciones tácticas.

## Ejemplo de ficha resumida

```text
Badger
N Small animal
Init +1; Senses low-light vision, scent; Perception +5

AC 13, touch 12, flat-footed 12 (+1 Dex, +1 natural, +1 size)
HP 11 (1d8+7)
Fort +6, Ref +3, Will +1

Speed 30 ft., burrow 10 ft.
Melee bite +3 (1d3+2), 2 claws +3 (1d2+2)
Space 5 ft., Reach 5 ft.
Special Attacks blood rage
```

---

# Botones globales del grupo

Cada bloque de criatura debe tener estos botones:

- `Atacar con todas`;
- `Tirar TS`;
- `Expandir ficha`.

No debe haber botones separados para Fortaleza, Reflejos y Voluntad en el MVP.

La acción `Tirar TS` tira las tres salvaciones para cada criatura del grupo.

## Atacar con todas

El botón `Atacar con todas` tira todos los ataques de todas las criaturas individuales de ese tipo.

La aplicación no necesita permitir ataques individuales en el MVP.

Debe mostrar claramente:

- número o identificador de criatura;
- nombre del ataque;
- tirada de ataque;
- daño normal;
- daño crítico si procede;
- tipos de daño separados.

Ejemplo:

```text
Fiery Badger 1
Bite: ataque 17 | daño 5 piercing + 1 fire
Claw 1: ataque 13 | daño 3 slashing + 1 fire
Claw 2: ataque 21 | daño 4 slashing + 1 fire

Fiery Badger 2
Bite: ataque 19 | daño 4 piercing + 1 fire
Claw 1: ataque 10 | daño 3 slashing + 1 fire
Claw 2: ataque 16 | daño 2 slashing + 1 fire
```

La aplicación debe tirar ataque y daño siempre, aunque luego el usuario determine manualmente si el ataque impacta o no.

La aplicación no debe aplicar modificadores temporales ni situacionales.

## Críticos

Cuando un ataque amenace crítico, el resultado debe mostrar:

- tirada de amenaza;
- tirada de confirmación;
- daño normal;
- daño crítico.

Ejemplo:

```text
Fiery Badger 1
Bite
Amenaza: 20
Confirmación: 18
Daño normal: 5 piercing + 1 fire
Daño crítico: 10 piercing + 2 fire
```

El daño normal debe mostrarse siempre, incluso cuando haya crítico.

## Tirar TS

El botón `Tirar TS` tira las tres salvaciones para todas las criaturas individuales del grupo:

- Fortaleza;
- Reflejos;
- Voluntad.

Ejemplo:

```text
Fiery Badger 1: Fort 18 / Ref 12 / Will 9
Fiery Badger 2: Fort 21 / Ref 15 / Will 4
Fiery Badger 3: Fort 11 / Ref 19 / Will 13
```

No se necesitan botones separados para cada tirada de salvación.

## Expandir ficha

El botón `Expandir ficha` abre un modal o panel con la ficha completa de la criatura final.

Debe mostrar la ficha ya transformada:

- con `Augment Summoning` aplicado;
- con plantilla aplicada, si procede;
- con `Deep Guardian` aplicado, si procede.

No es necesario mostrar la ficha base sin transformar.

Si la pantalla consume un campo técnico de texto completo, debe usar `fullStatBlock`. En este contexto debe corresponder a la ficha final transformada (`ResolvedCreature.fullStatBlock`) o a una composición equivalente de campos estructurados finales.

El botón no debe llamarse `Expandir habilidades`, porque la ficha expandida puede incluir toda la información disponible, no solo habilidades especiales.

---

# Cards de criaturas activas

Debajo de la ficha resumida de cada grupo deben aparecer las criaturas individuales activas.

Aunque visualmente puedan organizarse como tabla en pantallas grandes, en tablet vertical deben funcionar preferentemente como **cards pequeñas**.

Cada card representa una criatura concreta.

Debe mostrar:

- identificador de criatura;
- PG máximos;
- PG actuales;
- botón `Dañar`;
- botón `Curar`;
- botón `Eliminar`.

Ejemplo en formato conceptual:

| Criatura | PG máx | PG actuales | Acciones |
|---|---:|---:|---|
| Fiery Badger 1 | 15 | 15 | Dañar / Curar / Eliminar |
| Fiery Badger 2 | 15 | 7 | Dañar / Curar / Eliminar |
| Fiery Badger 3 | 15 | -2 | Dañar / Curar / Eliminar |

En tablet vertical, esta información puede representarse así:

```text
Fiery Badger 1
PG: 15 / 15
[Dañar] [Curar] [Eliminar]

Fiery Badger 2
PG: 7 / 15
[Dañar] [Curar] [Eliminar]

Fiery Badger 3
PG: -2 / 15 · Caída
[Dañar] [Curar] [Eliminar]
```

---

# Estados visuales de las cards

La aplicación no debe gestionar un sistema completo de condiciones.

Solo debe distinguir visualmente y por API los tres estados derivados de PG actuales y máximos.

## Criatura sana

Estado técnico: `HEALTHY`.

Se aplica cuando los PG actuales son iguales a los PG máximos.

Ejemplo:

```text
PG: 15 / 15 · Sana
```

## Criatura dañada

Estado técnico: `DAMAGED`.

Se aplica cuando los PG actuales son mayores que 0 y menores que los PG máximos.

Ejemplo:

```text
PG: 12 / 15 · Dañada
```

## Criatura caída

Estado técnico: `DOWN`.

Si una criatura llega a 0 PG o menos, debe marcarse como caída.

Ejemplo:

```text
PG: -2 / 15 · Caída
```

La aplicación no debe eliminarla automáticamente.

El usuario puede seguir:

- dañándola;
- curándola;
- eliminándola manualmente.

Eliminar una criatura no cambia su estado a otro valor: la instancia desaparece del grupo. Si el grupo se queda sin instancias, el grupo desaparece.

Esto es importante porque algunas criaturas pueden seguir actuando o existiendo por debajo de 0 PG.

---

# Dañar criatura

El botón `Dañar` abre un modal para aplicar daño a una criatura individual.

El modal debe mostrar:

- nombre de la criatura;
- PG actuales;
- PG máximos;
- botones rápidos de daño;
- campo para introducir daño manual como entero positivo mayor o igual que 1;
- notas defensivas relevantes.

Botones rápidos recomendados:

```text
-1
-5
-10
```

Estos botones son etiquetas de interfaz. El endpoint de daño recibe siempre un `amount` positivo, por ejemplo `{ "amount": 5 }`; la resta de PG la determina `/damage`.

El usuario introduce el daño final que quiere aplicar como entero positivo mayor o igual que 1. Si introduce `0`, un número negativo, decimal, texto, `null`, vacío o no informa el campo, la aplicación debe mostrar error y no modificar PG.

La aplicación no debe calcular automáticamente reducción de daño, resistencias ni inmunidades.

## Notas defensivas en el modal de daño

Si la criatura tiene defensas relevantes, deben mostrarse en el modal de daño.

Ejemplos:

```text
RD 5/good
Resistencia fire 10
Inmune poison
```

Estas notas sirven para que el usuario calcule manualmente el daño final antes de aplicarlo.

---

# Curar criatura

El botón `Curar` abre un modal o control equivalente para recuperar PG de una criatura individual.

Debe mostrar:

- nombre de la criatura;
- PG actuales;
- PG máximos;
- botones rápidos de curación;
- campo para introducir curación manual como entero positivo mayor o igual que 1.

Botones rápidos recomendados:

```text
+1
+5
+10
```

Estos botones son etiquetas de interfaz. El endpoint de curación recibe siempre un `amount` positivo, por ejemplo `{ "amount": 5 }`; la suma de PG la determina `/heal`.

El usuario introduce la curación como entero positivo mayor o igual que 1. Si introduce `0`, un número negativo, decimal, texto, `null`, vacío o no informa el campo, la aplicación debe mostrar error y no modificar PG.

La curación no debe superar los PG máximos salvo que se decida expresamente permitirlo por configuración.

Para el MVP, la curación debe quedar limitada a los PG máximos.

---

# Eliminar criatura

El botón `Eliminar` elimina una criatura individual.

Debe estar visible siempre.

Casos de uso:

- criatura muerta;
- criatura retirada;
- criatura irrelevante;
- corrección manual.

La aplicación debe pedir confirmación antes de eliminar una criatura individual, salvo que en pruebas de mesa se vea que esa confirmación ralentiza demasiado.

La aplicación nunca debe borrar criaturas automáticamente por llegar a 0 PG o menos.

---

# Resultado de tiradas

La pantalla principal debe mostrar el resultado más reciente de una tirada, si existe.

Junto al resultado visible debe existir una acción discreta para limpiar solo ese resultado, por ejemplo un botón pequeño:

```text
Limpiar resultado
```

Esta acción no debe saturar la pantalla ni competir visualmente con las acciones principales de combate.

Puede mostrarse en:

- modal;
- panel inferior;
- zona destacada bajo la cabecera;
- zona destacada dentro del bloque de criatura correspondiente.

El MVP no necesita historial completo de tiradas.

Al pulsar `Limpiar resultado`, desaparece el resultado actual de la pantalla. Esta acción no elimina grupos, no elimina instancias, no cambia PG, no modifica usos diarios y no modifica configuración.

Si el usuario necesita repetir una tirada, puede pulsar de nuevo el botón correspondiente.

---

# Comportamiento esperado

## Al invocar

Cuando se invoca una criatura:

1. Se calcula el número de criaturas convocadas.
2. Se aplica la plantilla si procede.
3. Se aplican las reglas fijas del personaje.
4. Se crea o actualiza el grupo correspondiente.
5. Se crean las cards individuales necesarias.
6. Se descuentan usos diarios si `remaining > 0`; si está a 0, se mantiene en 0.
7. Se persiste el estado.

## Al dañar

Cuando se daña una criatura:

1. El usuario pulsa `Dañar` en una card individual.
2. La aplicación muestra notas defensivas relevantes, si existen.
3. El usuario introduce el daño final.
4. La aplicación resta PG.
5. Si los PG quedan a 0 o menos, la card se marca como caída.
6. La criatura permanece en pantalla.
7. Se persiste el estado.

## Al curar

Cuando se cura una criatura:

1. El usuario pulsa `Curar` en una card individual.
2. El usuario introduce la curación.
3. La aplicación suma PG hasta el máximo permitido.
4. Si la criatura estaba caída y pasa a tener más de 0 PG, deja de mostrarse como caída.
5. El estado se recalcula como `HEALTHY` si queda a PG máximos o `DAMAGED` si queda por encima de 0 y por debajo de PG máximos.
6. Se persiste el estado.

## Al eliminar

Cuando se elimina una criatura:

1. El usuario pulsa `Eliminar`.
2. La aplicación pide confirmación.
3. La criatura desaparece del grupo.
4. Si el grupo se queda sin criaturas, el grupo desaparece de la pantalla.
5. Se persiste el estado.

## Al limpiar invocaciones

Cuando se pulsa `Limpiar invocaciones`:

1. La aplicación pide confirmación.
2. Se eliminan todas las criaturas activas.
3. La pantalla queda vacía de grupos activos.
4. Se conservan configuración, usos diarios, últimas usadas y más usadas según corresponda.
5. Se persiste el estado.

---

# Requisitos de usabilidad

La pantalla principal debe priorizar:

- lectura rápida;
- botones grandes;
- pocos pasos;
- funcionamiento cómodo en vertical;
- ausencia de información secundaria innecesaria;
- modo oscuro;
- persistencia inmediata tras cada cambio relevante.

Debe evitar:

- menús profundos;
- tablas demasiado anchas en vertical;
- listas largas de botones tácticos;
- depender de hover o interacciones propias de escritorio;
- obligar al usuario a consultar la ficha completa para hacer acciones básicas.

---

# Decisiones específicas de esta pantalla

- La pantalla se organiza por grupos de criatura final activa.
- Las criaturas individuales se muestran como cards, aunque puedan representarse como tabla en pantallas amplias.
- No se muestran notas tácticas breves en la ficha resumida.
- No hay botones separados de Fortaleza, Reflejos y Voluntad.
- Hay un único botón `Tirar TS`.
- El botón de ficha se llama `Expandir ficha`.
- La ficha expandida muestra toda la ficha final transformada.
- El botón `Eliminar` está visible siempre.
- Las criaturas a 0 PG o menos se marcan como caídas pero no se borran automáticamente.
- El daño y la curación se aplican siempre a criaturas individuales.
- El estado se persiste tras cada cambio relevante.
