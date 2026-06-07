# AGENTS.md

Guía para agentes de IA, asistentes de código y herramientas de vibe coding que trabajen en este proyecto.

Este documento es una referencia operativa. Debe ayudar a tomar decisiones coherentes sin reabrir discusiones de producto ni introducir automatizaciones fuera de alcance.

---

## Shell environment

This project is being worked on from Windows using PowerShell / pwsh.

When running shell commands, use PowerShell-compatible syntax only.

Do not use Unix/Linux shell commands unless explicitly invoking bash, Git Bash, or WSL.

### Command equivalents

Use these PowerShell forms:

- List files:
  - Use: `Get-ChildItem`
  - Use: `Get-ChildItem -Force`
  - Do not use: `ls -la`

- Print current directory:
  - Use: `Get-Location`
  - Do not use: `pwd` unless only displaying the current path and it works in PowerShell.

- Remove files:
  - Use: `Remove-Item <path>`
  - Use: `Remove-Item -Recurse -Force <path>`
  - Do not use: `rm -rf`

- Copy files:
  - Use: `Copy-Item <source> <destination>`
  - Do not use: `cp -r`

- Move or rename files:
  - Use: `Move-Item <source> <destination>`
  - Do not use: `mv` if flags are needed.

- Create directories:
  - Use: `New-Item -ItemType Directory -Force -Path <path>`
  - Do not use: `mkdir -p`

- Search text:
  - Prefer: `Select-String -Path <path> -Pattern "<pattern>"`
  - For repository search, prefer `rg` if available.
  - Do not use: `grep -R`

- Show file content:
  - Use: `Get-Content <path>`
  - Use: `Get-Content <path> -TotalCount 100`
  - Do not use: `cat <path> | head`

- Environment variables:
  - Use: `$env:NAME`
  - Do not use: `$NAME` or `export NAME=value`

- Command chaining:
  - Use PowerShell syntax.
  - Avoid Bash-only constructs such as `&&`, `||`, `2>/dev/null`, `$(...)` when not valid in PowerShell.

Before running a command, ensure it is valid PowerShell syntax.

# 1. Contexto del proyecto

Este proyecto es una **aplicación web local** para gestionar en mesa las invocaciones de un personaje de **Pathfinder 1e**:

- Clase: **Inquisidor**.
- Raza: **enano**.
- Alineamiento: **Neutral Bueno**.
- Arquetipo: **Monster Tactician**.
- Uso: personal, local y presencial.
- Dispositivo principal: **tablet Android**.
- Contexto de uso: durante combates en partida, con ficha, dados físicos, papel y lápices en mesa.

El objetivo no es crear una herramienta genérica de Pathfinder, sino una herramienta rápida y práctica para este personaje concreto.

---

# 2. Objetivo principal del producto

La aplicación debe permitir manejar criaturas invocadas de forma rápida durante la partida.

Debe ayudar a:

- invocar criaturas disponibles;
- calcular cuántas criaturas aparecen;
- aplicar reglas fijas del personaje;
- agrupar criaturas activas por tipo final;
- gestionar PG individuales;
- tirar ataques y daños de todas las criaturas de un grupo;
- tirar las tres TS de todas las criaturas de un grupo;
- consultar la ficha completa final cuando haga falta.

El criterio de éxito es la mesa:

> La aplicación es correcta si reduce el tiempo de gestión de invocaciones y evita tener que consultar constantemente un documento largo de referencia.

---

# 3. Fuentes funcionales de verdad

Antes de modificar comportamiento funcional, consultar los documentos de `docs/`.

Orden recomendado:

1. `docs/01-vision-producto.md`
2. `docs/02-alcance-funcional.md`
3. `docs/03-pantalla-principal.md`
4. `docs/04-catalogo-criaturas.md`
5. `docs/05-reglas-tiradas.md`
6. `docs/07-casos-uso.md`
7. `docs/08-criterios-aceptacion.md`
8. `docs/09-glosario.md`

El documento `docs/06-*` se ignora por ahora si existe, porque está fuera del alcance del MVP.

Si hay contradicción entre documentos, aplicar este criterio:

1. Respetar decisiones explícitas del MVP.
2. Priorizar rapidez en mesa.
3. No automatizar reglas contextuales.
4. Preferir una solución simple y reversible.
5. No inventar reglas de Pathfinder.

---

# 4. Prioridades de diseño

El orden de prioridad del producto es:

1. **Rapidez en mesa.**
2. **Claridad visual.**
3. **Pocas pulsaciones.**
4. **Persistencia fiable del estado.**
5. **Reglas simples y explícitas.**
6. **Evitar automatizaciones excesivas.**
7. **Evitar sobreingeniería.**

Una funcionalidad técnicamente elegante pero lenta en mesa es mala para este proyecto.

Una funcionalidad incompleta pero clara, rápida y segura puede ser aceptable para el MVP.

---

# 5. Reglas de sistema de juego

## Sistema correcto

Usar exclusivamente:

```text
Pathfinder 1e
```

## Sistemas prohibidos

No usar reglas, términos ni estructuras de:

- Pathfinder 2e.
- D&D 5e.
- Starfinder.
- Otros sistemas d20.

No introducir conceptos como:

- ventaja/desventaja;
- bonus action;
- proficiency bonus;
- acciones de tres puntos;
- condiciones de PF2e;
- concentración estilo D&D 5e.

---

# 6. Alcance del MVP

El MVP debe incluir:

- pantalla principal de combate;
- invocación de criaturas desde JSON;
- aplicación de reglas fijas del personaje;
- agrupación de criaturas por tipo final;
- cards individuales con PG independientes;
- daño individual;
- curación individual;
- eliminación individual;
- limpieza de todas las invocaciones activas;
- contador de usos diarios;
- ataques globales por grupo;
- tiradas de TS globales por grupo;
- ficha expandida;
- persistencia del estado;
- modo oscuro;
- diseño responsive, priorizando tablet Android en vertical.

---

# 7. Fuera de alcance del MVP

No implementar en el MVP salvo petición explícita:

- login;
- usuarios;
- multijugador;
- seguridad;
- integración con Fantasy Grounds;
- gestión de iniciativa;
- gestión de enemigos;
- mapa táctico;
- posicionamiento;
- medición de distancias;
- duración exacta en asaltos;
- gestión completa del personaje;
- base de datos completa de Pathfinder;
- importación automática desde fuentes externas;
- diagrama de elección de invocación;
- automatización de estados completos;
- automatización de buffs/debuffs;
- modificadores temporales;
- ataques de oportunidad;
- carga;
- flanqueo;
- cobertura;
- ocultación;
- inspiración de bardo;
- bendiciones;
- uso de hechizos de criaturas invocadas;
- auras;
- ataques especiales contextuales;
- cálculo automático de RD, resistencias o inmunidades.

---

# 8. Principio clave de reglas

La aplicación debe hacer operaciones repetitivas simples, no resolver Pathfinder entero.

Debe poder:

- tirar dados;
- sumar bonificadores fijos;
- calcular cantidad de criaturas;
- aplicar reglas fijas del personaje;
- mostrar daño separado por tipo;
- detectar amenaza de crítico;
- tirar confirmación;
- mostrar daño normal y crítico;
- tirar TS.

No debe:

- decidir si un ataque impacta;
- decidir si un crítico confirma;
- aplicar daño automáticamente a enemigos;
- aplicar daño automático desde enemigos;
- resolver estados;
- aplicar modificadores temporales;
- interpretar reglas ambiguas;
- decidir por el jugador o por el máster.

Si una regla depende del contexto de mesa, no automatizarla.

---

# 9. Reglas fijas del personaje

Estas reglas sí forman parte de la lógica funcional.

## 9.1. Nivel máximo de Summon Monster

Debe existir una configuración:

```text
maxSummonMonsterLevel
```

Inicialmente:

```text
maxSummonMonsterLevel = 3
```

Sirve para determinar disponibilidad y cantidad de criaturas.

## 9.2. Cantidad de criaturas invocadas

Regla funcional:

| Relación con nivel máximo | Cantidad |
|---|---:|
| Nivel máximo | 1 |
| Nivel máximo - 1 | `1d3 + 1` |
| Nivel máximo - 2 | `1d4 + 2` |

La aplicación calcula la cantidad automáticamente al invocar.

El usuario no introduce manualmente la cantidad en el MVP.

## 9.3. Augment Summoning

Toda criatura invocada recibe:

```text
+4 Fuerza
+4 Constitución
```

La ficha base del JSON no tiene por qué venir ya aumentada.

La aplicación debe generar la criatura final aplicando esta mejora.

## 9.4. Superior Summoning

Siempre que se tira cantidad de criaturas, se suma 1 criatura adicional.

En la práctica está incluido en:

```text
1d3 + 1
1d4 + 2
```

## 9.5. Versatile Summon Monster

Si la criatura lo permite, se puede aplicar una plantilla:

- `chthonic`;
- `fiery`;
- `celestial`;
- `entropic`;
- `resolute`.

Si la invocación se selecciona manualmente y hay varias plantillas posibles, preguntar plantilla.

Si se selecciona desde últimas usadas o más usadas, reutilizar directamente la combinación criatura + plantilla.

## 9.6. Deep Guardian

Después de aplicar plantilla, si la criatura final cumple al menos una condición:

- tiene velocidad `burrow`;
- tiene subtipo `earth`;

recibe:

```text
+1 al ataque
+1 a la CA
```

---

# 10. Modelo funcional de criaturas

## Criatura base

Es la ficha original cargada desde JSON.

No debe asumirse que incluye:

- Augment Summoning;
- plantilla aplicada;
- Deep Guardian.

## Criatura final

Es la ficha transformada que se usa en mesa.

Debe incluir:

- reglas fijas aplicadas;
- plantilla si procede;
- valores finales funcionales.

La pantalla principal, tiradas y ficha expandida deben usar la criatura final.

## Grupo de criaturas

Agrupa criaturas activas con la misma criatura final.

Ejemplo:

```text
Fiery Badger x5
```

Si se invoca dos veces la misma criatura con la misma plantilla, se añade al grupo existente.

Si cambia la plantilla, debe ser otro grupo.

## Instancia de criatura

Cada criatura concreta dentro de un grupo.

Todas las instancias de un grupo comparten ficha final, pero tienen PG actuales independientes.

Ejemplo:

```text
Fiery Badger 1 — PG 15 / 15
Fiery Badger 2 — PG 7 / 15
Fiery Badger 3 — PG -2 / 15
```

---

# 11. Pantalla principal

La pantalla principal es el centro de la aplicación.

Debe mostrar:

- `Invocar`;
- `Limpiar invocaciones`;
- contador de usos diarios;
- grupos de criaturas activas;
- resultado de tirada más reciente, si existe.

Cada grupo debe mostrar:

- nombre final;
- número de criaturas activas;
- ficha resumida;
- botón `Atacar con todas`;
- botón `Tirar TS`;
- botón `Expandir ficha`;
- cards individuales.

Cada card individual debe mostrar:

- identificador;
- PG actuales;
- PG máximos;
- `Dañar`;
- `Curar`;
- `Eliminar`.

El botón `Eliminar` debe estar visible siempre.

---

# 12. Ficha resumida

La ficha resumida debe mostrar datos mecánicos suficientes para mesa.

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
- daño de ataques;
- espacio;
- alcance;
- ataques especiales;
- número de criaturas activas.

No mostrar notas tácticas en la ficha resumida del MVP.

---

# 13. Ficha expandida

El botón debe llamarse:

```text
Expandir ficha
```

No usar:

```text
Expandir habilidades
```

La ficha expandida debe mostrar la ficha completa de la criatura final.

No es necesario mostrar la ficha base sin transformar.

---

# 14. Daño y curación

El daño y la curación son siempre individuales.

No aplicar daño a todo el grupo.

## Dañar

Al pulsar `Dañar`:

- abrir modal o control equivalente;
- mostrar PG actuales y máximos;
- permitir botones rápidos;
- permitir cantidad manual como entero positivo mayor o igual que 1;
- mostrar defensas especiales si existen.

Defensas especiales:

- RD;
- resistencias;
- inmunidades;
- vulnerabilidades.

La aplicación las muestra como aviso, pero no las aplica automáticamente.

Regla operativa para daño y curación:

- No enviar cantidades negativas a `/damage` ni a `/heal`.
- El payload debe usar siempre `amount` como entero positivo `>= 1`.
- `/damage` resta ese valor.
- `/heal` suma ese valor.
- `0`, negativos, decimales, texto, `null`, vacío o ausencia de `amount` son inválidos.
- Si la validación falla, no modificar PG, no cambiar estado y no persistir cambios.

## Curar

Al pulsar `Curar`:

- permitir botones rápidos;
- permitir cantidad manual como entero positivo mayor o igual que 1;
- sumar PG;
- no superar PG máximos en MVP.

## Estados de instancia

Usar únicamente estos estados visibles/API para una instancia:

- `HEALTHY`: `currentHitPoints == maxHitPoints`.
- `DAMAGED`: `currentHitPoints > 0 && currentHitPoints < maxHitPoints`.
- `DOWN`: `currentHitPoints <= 0`.

No usar `ACTIVE` como estado de instancia.

No usar `REMOVED` como estado visible, de API ni persistido. Al eliminar una criatura, se borra la instancia del grupo; si el grupo queda vacío, desaparece.

Si una criatura queda a 0 PG o menos:

- marcarla visualmente como caída (`DOWN`);
- no eliminarla automáticamente;
- permitir seguir dañando, curando o eliminando.

Si una criatura caída se cura por encima de 0 PG, recalcular su estado como `DAMAGED` o `HEALTHY` según sus PG actuales.

---

# 15. Tiradas de ataque

El botón del grupo debe llamarse:

```text
Atacar con todas
```

Al pulsarlo:

1. Buscar todas las instancias del grupo.
2. Tirar todos los ataques de cada instancia.
3. Tirar daño de cada ataque.
4. Mostrar resultados agrupados por criatura.
5. Mostrar daño como `Daño si impacta`.
6. No decidir impacto.
7. No aplicar daño automáticamente.

El resultado debe mostrar dado, modificador y total.

Ejemplo:

```text
Bite: d20 14 + 4 = 18
Daño si impacta: 1d3+3 = 5 piercing + 1 fire
```

Si un ataque tiene `cantidad: 2`, tirar dos ataques separados:

```text
Claw 1
Claw 2
```

---

# 16. Críticos

La aplicación debe detectar amenaza de crítico.

Si hay amenaza, debe mostrar:

- tirada de amenaza;
- tirada de confirmación;
- daño normal;
- daño crítico.

El daño normal debe mostrarse siempre aunque haya crítico.

La aplicación no decide si el crítico confirma contra la CA enemiga.

---

# 17. Daños separados por tipo

Los daños deben mantenerse separados por tipo.

Correcto:

```text
5 piercing + 1 fire
```

Incorrecto:

```text
6 damage
```

Esto es importante para resistencias, inmunidades y RD.

---

# 18. Tiradas de salvación

El botón del grupo debe llamarse:

```text
Tirar TS
```

Debe tirar siempre las tres salvaciones para cada criatura del grupo:

- Fortaleza;
- Reflejos;
- Voluntad.

No crear botones separados de Fort, Ref y Vol en el MVP.

Ejemplo:

```text
Fiery Badger 1
Fortaleza: d20 12 + 8 = 20
Reflejos: d20 7 + 3 = 10
Voluntad: d20 15 + 1 = 16
```

---

# 19. Persistencia

Persistir el estado tras cada cambio relevante.

Debe persistirse:

- invocaciones activas;
- grupos;
- instancias;
- PG actuales;
- usos diarios máximos;
- usos diarios restantes;
- configuración;
- últimas usadas;
- más usadas.

Regla operativa de usos diarios:

- No permitir estados persistidos con `remaining < 0`.
- No permitir estados persistidos con `remaining > maximum`.
- `maximum` debe ser `>= 0`.
- Al invocar con `remaining = 0`, no bloquear rígidamente en el MVP, pero no descontar por debajo de 0.
- Sumar usos debe acotarse a `maximum`.
- Restar usos debe acotarse a 0.
- Resetear usos deja `remaining = maximum`.
- Si se reduce `maximum`, ajustar `remaining = min(remaining, maximum)`.

No es obligatorio persistir historial completo de tiradas.

Sí debe mostrarse el resultado actual o más reciente hasta que se haga otra tirada o se limpie.

---

# 20. Stack técnico

Stack obligatorio o preferente:

- Backend: **Java + Spring Boot**.
- API: **REST**.
- Frontend: aplicación web responsive para tablet Android.
- Base de datos: sencilla y local.
- Uso: local personal.

No añadir seguridad, login ni multijugador.

## Base de datos

Usar una solución simple.

Opciones razonables:

- H2;
- SQLite;
- almacenamiento embebido equivalente.

Evitar dependencias pesadas si no aportan valor al MVP.

## Frontend

Priorizar:

- responsive real para tablet;
- orientación vertical;
- botones grandes;
- modo oscuro;
- pocos pasos;
- legibilidad;
- no depender de hover;
- uso táctil cómodo.
- Implementado con Vue 3.

No construir una interfaz de escritorio compleja y luego intentar adaptarla.

---

# 21. Arquitectura recomendada

Mantener separación clara entre:

- lógica de dominio;
- persistencia;
- API REST;
- interfaz;
- datos de criaturas.

Recomendación de módulos o paquetes:

```text
domain
application
infrastructure
api
```

Ejemplo conceptual:

```text
domain/
  creature/
  summon/
  dice/
  combat/
  rules/

application/
  summon/
  activecreature/
  rolls/

infrastructure/
  persistence/
  catalog/

api/
  controllers/
  dto/
```

No es obligatorio seguir exactamente esta estructura si el proyecto ya tiene otra, pero sí mantener separación de responsabilidades.

---

# 22. Lógica de dominio

La lógica importante debe estar fuera de controladores y componentes visuales.

Debe ser testeable sin levantar toda la aplicación.

Lógica que debe estar en dominio o servicios puros:

- tirar dados;
- calcular cantidad de criaturas;
- aplicar Augment Summoning;
- aplicar plantilla;
- aplicar Deep Guardian;
- generar criatura final;
- agrupar criaturas;
- aplicar daño;
- aplicar curación;
- calcular estado de instancia (`HEALTHY`, `DAMAGED`, `DOWN`) a partir de PG;
- generar tiradas de ataque;
- generar tiradas de TS;
- detectar amenaza de crítico;
- calcular daño crítico.

---

# 23. Datos de criaturas

Los datos de criaturas deben estar separados de la lógica.

Las criaturas se cargan desde JSON mantenidos manualmente.

La aplicación asume que los JSON son correctos.

No implementar importación automática desde webs o fuentes externas en el MVP.

No mezclar datos de criatura con código Java salvo que sea estrictamente necesario para tests.

---

# 24. Tests

Crear tests para lógica de dominio.

Priorizar tests de:

- parser/evaluador de dados;
- tiradas deterministas con generador controlado;
- cantidad de criaturas por nivel;
- Augment Summoning;
- Deep Guardian;
- agrupación por criatura final;
- PG independientes por instancia;
- daño individual;
- curación individual;
- cálculo de estado `HEALTHY`, `DAMAGED` y `DOWN`;
- ataques con cantidad;
- daño separado por tipo;
- amenaza de crítico;
- confirmación de crítico;
- daño normal y daño crítico;
- TS globales por grupo;
- persistencia de estado si procede.

Usar mocks o generadores deterministas para dados en tests.

No basar tests en aleatoriedad real.

---

# 25. Estilo de implementación

Priorizar:

- código simple;
- nombres claros;
- funciones pequeñas;
- dominio testeable;
- DTOs explícitos;
- errores comprensibles;
- mínima magia;
- baja sobreingeniería;
- implementación incremental.

Evitar:

- abstracciones prematuras;
- frameworks innecesarios;
- capas excesivas;
- generalizar para otros personajes;
- construir motor universal de Pathfinder;
- añadir sistemas no pedidos;
- optimizar antes de tener MVP.

---

# 26. Convenciones de lenguaje visible

La interfaz visible debe estar en español.

Usar:

- `PG`, no `HP`.
- `CA`, no `AC`.
- `TS`, no `saving throws`.
- `Fortaleza`, `Reflejos`, `Voluntad`.
- `Atacar con todas`.
- `Tirar TS`.
- `Expandir ficha`.
- `Dañar`.
- `Curar`.
- `Eliminar`.
- `Limpiar invocaciones`.

Los datos internos pueden usar inglés si facilita compatibilidad con fichas y JSON, pero la UI debe priorizar español.

---

# 27. Accesibilidad y usabilidad táctil

La app se usará en mesa durante combate.

Por tanto:

- botones grandes;
- buen contraste;
- modo oscuro;
- layouts no saturados;
- evitar tablas anchas en vertical;
- cards antes que tablas rígidas en móvil/tablet;
- evitar menús profundos;
- acciones frecuentes visibles;
- confirmaciones solo donde eviten pérdidas importantes;
- no usar interacciones dependientes de ratón.

Aplicar daño con botones rápidos debe poder hacerse en menos de 3 pulsaciones.

Atacar con todo un grupo debe hacerse con 1 pulsación.

---

# 28. Confirmaciones

Pedir confirmación para:

- `Limpiar invocaciones`;
- eliminar una criatura individual, salvo que se decida quitarlo tras pruebas de mesa.

No pedir confirmaciones innecesarias en acciones frecuentes si ralentizan la mesa.

---

# 29. Criterios de aceptación básicos

El MVP debe poder demostrar:

- abrir pantalla principal;
- invocar criatura desde JSON;
- aplicar reglas fijas;
- generar cantidad automáticamente;
- crear instancias separadas;
- compartir ficha de tipo;
- mantener PG independientes;
- dañar una criatura;
- curar una criatura;
- marcar caída;
- eliminar una criatura;
- limpiar invocaciones;
- tirar ataques de todo un grupo;
- mostrar dado, modificador y total;
- mostrar daño separado del ataque;
- mostrar daño separado por tipo;
- gestionar amenaza y confirmación de crítico;
- mostrar daño normal y crítico;
- tirar TS de todo un grupo;
- expandir ficha final;
- persistir estado tras recargar.

---

# 30. Comandos de desarrollo

No inventar comandos si el proyecto aún no los define.

Cuando exista wrapper de build, preferir:

```bash
./mvnw test
```

o:

```bash
./gradlew test
```

según el proyecto use Maven o Gradle.

Antes de proponer comandos, revisar la estructura real del repositorio.

---

# 31. Qué hacer ante dudas

Si una decisión afecta al alcance funcional o a reglas de Pathfinder:

1. Revisar `docs/`.
2. Elegir la opción más simple.
3. No automatizar reglas contextuales.
4. No introducir sistemas nuevos.
5. Dejar la duda documentada si no bloquea.
6. Preguntar solo si la decisión cambia el comportamiento de producto.

Si la duda es técnica y no cambia producto, tomar una decisión razonable y mantenerla simple.

---

# 32. Prohibiciones importantes

No hacer estas cosas sin petición explícita:

- convertir la app en genérica para cualquier clase;
- añadir login;
- añadir roles;
- añadir permisos;
- añadir multijugador;
- añadir integración con Fantasy Grounds;
- añadir importadores externos;
- añadir control de iniciativa;
- añadir enemigos;
- añadir mapa;
- añadir duración por asaltos;
- añadir sistema completo de condiciones;
- añadir modificadores temporales;
- añadir motor completo de reglas Pathfinder;
- usar reglas de PF2e, D&D 5e o Starfinder;
- aplicar automáticamente daño a objetivos;
- decidir si un ataque impacta;
- ocultar daño normal cuando haya crítico;
- mezclar PG de instancias del mismo grupo.

---

# 33. Definición de terminado

Una tarea se considera terminada cuando:

- cumple el comportamiento funcional descrito en `docs/`;
- no introduce reglas fuera de alcance;
- mantiene la rapidez de uso en mesa;
- incluye tests si afecta a lógica de dominio;
- no rompe persistencia del estado;
- no empeora la experiencia en tablet Android;
- respeta nombres visibles en español;
- mantiene los datos de criaturas separados de la lógica.

