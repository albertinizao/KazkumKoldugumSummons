# Casos de uso

Este documento define los casos de uso funcionales del MVP.

Los casos de uso están orientados a implementar una aplicación rápida de mesa para gestionar criaturas invocadas. No describen reglas completas de Pathfinder 1e, sino interacciones concretas que la aplicación debe soportar.

---

# Actores

## Usuario

Persona que usa la aplicación en mesa para gestionar las invocaciones del Inquisidor Monster Tactician.

En el MVP solo existe un usuario.

No hay login, perfiles ni multijugador.

---

# Supuestos generales

Estos supuestos aplican a todos los casos de uso:

- La aplicación se usa desde una tablet Android, principalmente en vertical.
- Las criaturas disponibles proceden de JSON añadidos manualmente.
- La aplicación asume que los datos de criaturas son correctos.
- La aplicación aplica las reglas fijas del personaje.
- La aplicación persiste el estado tras cada cambio relevante.
- La aplicación no controla duración en asaltos.
- La aplicación no gestiona iniciativa.
- La aplicación no gestiona enemigos.
- La aplicación no aplica modificadores temporales.
- La aplicación no decide si un ataque impacta.
- La aplicación no aplica daño automáticamente a enemigos.
- La aplicación no resuelve estados complejos.

---

# CU-01 — Invocar una criatura

## Historia de usuario

Como usuario,  
quiero pulsar un botón de invocación,  
para añadir una o varias criaturas activas al combate.

## Precondiciones

- La aplicación está abierta.
- Existen criaturas cargadas en el catálogo JSON.
- Existe una configuración de nivel máximo de `Summon Monster`.
- Existe un contador de usos diarios.
- El usuario está en la pantalla principal.

## Disparador

El usuario pulsa el botón:

```text
Invocar
```

## Flujo principal

1. La aplicación abre el modal de invocación.
2. El usuario selecciona una criatura mediante una de estas opciones:
   - últimas usadas;
   - más usadas;
   - búsqueda manual.
3. Si el usuario elige desde últimas usadas o más usadas, la aplicación envía a la API el `shortcutId` de la entrada seleccionada y reutiliza la combinación previa de criatura + plantilla almacenada en ese shortcut.
4. Si el usuario elige mediante búsqueda manual y la criatura permite varias plantillas, la aplicación solicita elegir plantilla.
5. La aplicación genera la criatura final invocable aplicando:
   - `Augment Summoning`;
   - plantilla seleccionada, si procede;
   - `Deep Guardian`, si procede.

   `Augment Summoning` se aplica desde metadatos explícitos de la criatura base: `hitDice.count`, `fortitudeAbility`, `attackAbility`, `damageAbility` y `damageAbilityMultiplier`. La aplicación no infiere esos datos desde texto libre.

   `Deep Guardian`, si procede, suma `+1` al ataque, `+1` a la CA normal y `+1` a la CA desprevenida. No modifica la CA de toque.
6. La aplicación calcula automáticamente cuántas criaturas aparecen según el nivel de `Summon Monster`.
7. La aplicación crea las criaturas individuales necesarias.
8. La aplicación añade las criaturas a la pantalla principal.
9. La aplicación agrupa las criaturas con otras del mismo tipo final, si ya existían.
10. La aplicación descuenta automáticamente 1 uso diario.
11. La aplicación guarda el estado.

## Flujos alternativos

### A1 — La criatura no permite plantilla

1. El usuario selecciona una criatura.
2. La aplicación detecta que no tiene plantillas permitidas.
3. La aplicación no pregunta plantilla.
4. Continúa el flujo principal.

### A2 — Ya existe un grupo igual

1. El usuario invoca una criatura cuya versión final ya está activa.
2. La aplicación no crea un bloque nuevo.
3. Añade las nuevas criaturas individuales al grupo existente.

### A3 — Misma criatura base con distinta plantilla

1. El usuario invoca una criatura base ya activa, pero con otra plantilla.
2. La aplicación crea un grupo distinto.
3. Ambos grupos permanecen separados.

### A4 — Invocar con `remaining = 0`

1. El usuario intenta invocar una criatura cuando los usos diarios restantes están a 0.
2. Para el MVP, la aplicación no bloquea rígidamente la invocación, porque puede haber correcciones de mesa.
3. La aplicación crea la invocación si el resto de validaciones son correctas.
4. La aplicación mantiene `remaining = 0`.
5. La aplicación no descuenta por debajo de 0 y no persiste valores negativos.

## Resultado esperado

Las criaturas aparecen en la pantalla principal agrupadas por tipo final.

Ejemplo:

```text
Fiery Badger x5
```

Cada criatura individual aparece con su propia card de PG.

## Criterios de aceptación

- El usuario puede abrir el modal de invocación.
- El usuario puede elegir una criatura disponible.
- La aplicación pregunta plantilla cuando corresponde.
- La aplicación no pregunta plantilla al usar una combinación de últimas o más usadas, y la invocación se realiza mediante `shortcutId`.
- La aplicación calcula automáticamente la cantidad.
- La aplicación aplica reglas fijas del personaje.
- `Augment Summoning` se calcula mediante metadatos estructurados, no parseando texto libre.
- La aplicación crea cards individuales.
- La aplicación agrupa criaturas iguales con la misma plantilla.
- La aplicación separa criaturas iguales con distinta plantilla.
- La aplicación descuenta 1 uso diario.
- La aplicación persiste el estado.

---

# CU-02 — Aplicar daño

## Historia de usuario

Como usuario,  
quiero aplicar daño a una criatura concreta,  
para actualizar sus PG actuales rápidamente.

## Precondiciones

- Hay al menos una criatura activa en pantalla.
- La criatura tiene PG actuales.
- El usuario está en la pantalla principal.

## Disparador

El usuario pulsa el botón:

```text
Dañar
```

en una card individual.

## Flujo principal

1. La aplicación abre el modal de daño de esa criatura.
2. La aplicación muestra:
   - nombre de la criatura;
   - PG actuales;
   - PG máximos;
   - botones rápidos de daño;
   - campo de daño manual;
   - notas defensivas relevantes, si existen.
3. El usuario introduce la cantidad de daño final que quiere aplicar como entero positivo mayor o igual que 1.
4. La aplicación resta esa cantidad a los PG actuales.
5. La aplicación recalcula el estado visible/API de la instancia: `HEALTHY`, `DAMAGED` o `DOWN` según sus PG actuales y máximos.
6. Si la criatura queda a 0 PG o menos, se marca visualmente como caída (`DOWN`).
7. La criatura permanece en pantalla.
8. La aplicación guarda el estado.

## Flujos alternativos

### A1 — La criatura tiene RD, resistencia o inmunidad

1. La aplicación muestra esas defensas como nota en el modal.
2. El usuario calcula manualmente el daño final.
3. La aplicación aplica únicamente la cantidad válida introducida por el usuario.

### A2 — Daño deja la criatura por debajo de 0 PG

1. La aplicación permite PG negativos.
2. La criatura se marca como caída (`DOWN`).
3. La criatura puede seguir siendo dañada, curada o eliminada manualmente.

### A3 — El usuario cancela

1. El usuario cierra el modal sin confirmar.
2. La aplicación no modifica los PG.

### A4 — Cantidad de daño inválida

1. El usuario introduce una cantidad vacía, `0`, negativa, decimal, texto, `null` o no informa el campo.
2. La aplicación muestra un error funcional de cantidad inválida.
3. La aplicación no modifica los PG.
4. La aplicación no cambia el estado de la instancia.
5. La aplicación no persiste ningún cambio.

## Resultado esperado

Los PG actuales de la criatura se reducen y la pantalla refleja el nuevo valor.

Si queda a 0 PG o menos, se marca como caída (`DOWN`), pero no se elimina automáticamente.

## Criterios de aceptación

- El daño se aplica solo a la criatura seleccionada.
- La aplicación permite botones rápidos.
- La aplicación permite entrada manual.
- La cantidad de daño debe ser un entero positivo mayor o igual que 1.
- Si la cantidad de daño es inválida, la aplicación muestra error y no modifica PG.
- La aplicación muestra defensas relevantes como aviso.
- La aplicación no calcula RD, resistencias ni inmunidades.
- La aplicación permite PG a 0 o negativos.
- La aplicación recalcula el estado de instancia como `HEALTHY`, `DAMAGED` o `DOWN`.
- La aplicación no borra automáticamente la criatura.
- La aplicación persiste el estado.

---

# CU-03 — Curar una criatura

## Historia de usuario

Como usuario,  
quiero curar una criatura concreta,  
para actualizar sus PG actuales rápidamente.

## Precondiciones

- Hay al menos una criatura activa en pantalla.
- La criatura tiene PG actuales y PG máximos.
- El usuario está en la pantalla principal.

## Disparador

El usuario pulsa el botón:

```text
Curar
```

en una card individual.

## Flujo principal

1. La aplicación abre el modal de curación de esa criatura.
2. La aplicación muestra:
   - nombre de la criatura;
   - PG actuales;
   - PG máximos;
   - botones rápidos de curación;
   - campo de curación manual.
3. El usuario introduce la cantidad de curación como entero positivo mayor o igual que 1.
4. La aplicación suma esa cantidad a los PG actuales.
5. La aplicación limita los PG al máximo de la criatura.
6. Si la criatura estaba caída y pasa a tener más de 0 PG, deja de mostrarse como caída.
7. La aplicación recalcula el estado como `HEALTHY` si queda a PG máximos o `DAMAGED` si queda por encima de 0 y por debajo de PG máximos.
8. La aplicación guarda el estado.

## Flujos alternativos

### A1 — La curación supera los PG máximos

1. La aplicación suma la curación.
2. Si el resultado supera los PG máximos, deja los PG en el máximo.

### A2 — El usuario cancela

1. El usuario cierra el modal sin confirmar.
2. La aplicación no modifica los PG.

### A3 — Cantidad de curación inválida

1. El usuario introduce una cantidad vacía, `0`, negativa, decimal, texto, `null` o no informa el campo.
2. La aplicación muestra un error funcional de cantidad inválida.
3. La aplicación no modifica los PG.
4. La aplicación no cambia el estado de la instancia.
5. La aplicación no persiste ningún cambio.

## Resultado esperado

Los PG actuales de la criatura aumentan hasta un máximo igual a sus PG máximos.

## Criterios de aceptación

- La curación se aplica solo a la criatura seleccionada.
- La aplicación permite botones rápidos.
- La aplicación permite entrada manual.
- La cantidad de curación debe ser un entero positivo mayor o igual que 1.
- Si la cantidad de curación es inválida, la aplicación muestra error y no modifica PG.
- La curación no supera los PG máximos en el MVP.
- La marca de caída desaparece si la criatura vuelve a tener más de 0 PG.
- La criatura pasa a `HEALTHY` o `DAMAGED` según sus PG actuales.
- La aplicación persiste el estado.

---

# CU-04 — Eliminar una criatura individual

## Historia de usuario

Como usuario,  
quiero eliminar una criatura concreta,  
para retirar del combate criaturas muertas, desaparecidas o irrelevantes.

## Precondiciones

- Hay al menos una criatura activa en pantalla.
- El usuario está en la pantalla principal.

## Disparador

El usuario pulsa el botón:

```text
Eliminar
```

en una card individual.

## Flujo principal

1. La aplicación solicita confirmación.
2. El usuario confirma.
3. La aplicación elimina la criatura individual.
4. La eliminación no se representa como un estado visible/API ni persistido.
5. Si el grupo todavía tiene más criaturas, el grupo permanece visible.
6. Si el grupo se queda sin criaturas, el grupo desaparece.
7. La aplicación guarda el estado.

## Flujos alternativos

### A1 — El usuario cancela

1. La aplicación cierra la confirmación.
2. La criatura permanece en pantalla.

## Resultado esperado

La criatura seleccionada desaparece de la pantalla.

## Criterios de aceptación

- El botón `Eliminar` está visible siempre.
- La aplicación elimina solo la criatura seleccionada.
- La aplicación no elimina criaturas automáticamente por llegar a 0 PG.
- La aplicación elimina el grupo si se queda vacío.
- La aplicación persiste el estado.

---

# CU-05 — Tirar ataques de un grupo

## Historia de usuario

Como usuario,  
quiero tirar todos los ataques de todas las criaturas de un mismo tipo,  
para resolver rápidamente su turno.

## Precondiciones

- Hay al menos un grupo de criaturas activas.
- El grupo tiene al menos una criatura individual.
- La criatura final tiene ataques definidos.
- El usuario está en la pantalla principal.

## Disparador

El usuario pulsa el botón:

```text
Atacar con todas
```

en un grupo de criatura.

## Flujo principal

1. La aplicación busca todas las criaturas activas de ese grupo.
2. La aplicación obtiene los ataques de la ficha final transformada.
3. La aplicación tira todos los ataques de cada criatura individual.
4. La aplicación tira el daño de cada ataque.
5. Si algún ataque amenaza crítico, la aplicación tira confirmación.
6. Si algún ataque amenaza crítico, la aplicación tira daño crítico.
7. La aplicación muestra los resultados agrupados por criatura.
8. La aplicación muestra el daño como daño si impacta.
9. La aplicación no decide si los ataques impactan.
10. La aplicación no aplica daño automáticamente.

## Flujos alternativos

### A1 — Ataque con varios golpes iguales

1. El ataque tiene `quantity` mayor que 1.
2. La aplicación tira cada repetición por separado.
3. El resultado diferencia cada golpe.

Ejemplo:

```text
Claw 1
Claw 2
```

### A2 — Ataque con daño adicional

1. El ataque tiene varios componentes de daño.
2. La aplicación tira y muestra cada componente separado.

Ejemplo:

```text
5 piercing + 1 fire
```

### A3 — Ataque con amenaza de crítico

1. La tirada natural está dentro del rango de amenaza.
2. La aplicación muestra:
   - tirada de amenaza;
   - tirada de confirmación;
   - daño normal;
   - daño crítico.
3. El usuario decide manualmente si el crítico se confirma contra el enemigo.
4. Si el ataque tiene daño adicional no multiplicable, la aplicación lo muestra también en el daño crítico una vez, sin multiplicarlo.

Ejemplo:

```text
Daño normal: 6 piercing + 1 fire
Daño crítico: 12 piercing + 1 fire
```

## Resultado esperado

La aplicación muestra una lista de resultados agrupados por criatura.

Ejemplo:

```text
Fiery Badger 1
Bite: d20 14 + 6 = 20
Daño si impacta: 1d3+4 = 6 piercing + 1 fire

Claw 1: d20 8 + 6 = 14
Daño si impacta: 1d2+4 = 5 slashing + 1 fire

Claw 2: d20 19 + 6 = 25
Daño si impacta: 1d2+4 = 6 slashing + 1 fire
```

## Criterios de aceptación

- La aplicación tira ataques para todas las criaturas del grupo.
- La aplicación tira todos los ataques definidos.
- La aplicación respeta la cantidad de ataques.
- La aplicación tira daño junto al ataque.
- El daño se muestra como `daño si impacta`.
- El daño se muestra separado por tipo.
- La aplicación detecta amenaza de crítico.
- La aplicación tira confirmación.
- La aplicación muestra daño normal y daño crítico.
- La aplicación no decide impactos.
- La aplicación no aplica daño automáticamente.

---

# CU-06 — Tirar TS de un grupo

## Historia de usuario

Como usuario,  
quiero tirar las salvaciones de todas las criaturas de un mismo tipo,  
para resolver rápidamente efectos de área o salvaciones grupales.

## Precondiciones

- Hay al menos un grupo de criaturas activas.
- El grupo tiene al menos una criatura individual.
- La criatura final tiene salvaciones definidas.
- El usuario está en la pantalla principal.

## Disparador

El usuario pulsa el botón:

```text
Tirar TS
```

en un grupo de criatura.

## Flujo principal

1. La aplicación busca todas las criaturas activas de ese grupo.
2. La aplicación obtiene las salvaciones de la ficha final transformada.
3. La aplicación tira Fortaleza para cada criatura.
4. La aplicación tira Reflejos para cada criatura.
5. La aplicación tira Voluntad para cada criatura.
6. La aplicación muestra los resultados agrupados por criatura.
7. La aplicación no resuelve consecuencias del fallo o éxito.

## Resultado esperado

La aplicación muestra las tres salvaciones de cada criatura del grupo.

Ejemplo:

```text
Fiery Badger 1
Fortaleza: d20 12 + 6 = 18
Reflejos: d20 7 + 3 = 10
Voluntad: d20 15 + 1 = 16

Fiery Badger 2
Fortaleza: d20 4 + 6 = 10
Reflejos: d20 16 + 3 = 19
Voluntad: d20 2 + 1 = 3
```

También puede mostrarse de forma compacta:

```text
Fiery Badger 1: Fort 18 / Ref 10 / Will 16
Fiery Badger 2: Fort 10 / Ref 19 / Will 3
```

## Criterios de aceptación

- Hay un único botón `Tirar TS`.
- La aplicación tira Fortaleza, Reflejos y Voluntad.
- La aplicación tira para todas las criaturas del grupo.
- La aplicación muestra resultados agrupados por criatura.
- La aplicación no resuelve efectos ni estados.

---

# CU-07 — Expandir ficha de criatura

## Historia de usuario

Como usuario,  
quiero expandir la ficha de un tipo de criatura,  
para consultar detalles completos sin buscar en otro documento.

## Precondiciones

- Hay al menos un grupo de criaturas activas.
- La criatura final tiene ficha expandible.
- El usuario está en la pantalla principal.

## Disparador

El usuario pulsa el botón:

```text
Expandir ficha
```

en un grupo de criatura.

## Flujo principal

1. La aplicación abre un modal o panel de ficha expandida.
2. La aplicación muestra la ficha final transformada.
3. La ficha usa `ResolvedCreature.fullStatBlock` si existe como texto final transformado, o una composición equivalente de campos estructurados finales si no se puede generar texto libre completo.
4. La ficha incluye los datos disponibles tras aplicar:
   - `Augment Summoning`;
   - plantilla seleccionada, si procede;
   - `Deep Guardian`, si procede.
5. El usuario consulta la información.
6. El usuario cierra el modal o panel.

## Resultado esperado

El usuario puede consultar la ficha completa de la criatura final sin abandonar la pantalla principal.

## Criterios de aceptación

- El botón se llama `Expandir ficha`.
- La ficha mostrada es la ficha final transformada.
- La ficha expandida usa `ResolvedCreature.fullStatBlock` o una composición equivalente de campos estructurados finales.
- No es necesario mostrar la ficha base sin transformar.
- La ficha puede incluir habilidades, estadísticas completas, texto descriptivo y otros datos del catálogo.

---

# CU-08 — Limpiar todas las invocaciones activas

## Historia de usuario

Como usuario,  
quiero limpiar todas las invocaciones activas,  
para cerrar un combate o preparar la pantalla para otra escena.

## Precondiciones

- El usuario está en la pantalla principal.
- Puede haber cero, una o varias criaturas activas.

## Disparador

El usuario pulsa el botón:

```text
Limpiar invocaciones
```

## Flujo principal

1. La aplicación solicita confirmación.
2. El usuario confirma.
3. La aplicación elimina todos los grupos de criaturas activas.
4. La aplicación elimina todas las cards individuales activas.
5. La aplicación conserva configuración, usos diarios, últimas usadas y más usadas.
6. La aplicación guarda el estado.

## Flujos alternativos

### A1 — El usuario cancela

1. La aplicación cierra la confirmación.
2. No se elimina ninguna criatura.

## Resultado esperado

La pantalla principal queda sin invocaciones activas.

## Criterios de aceptación

- La aplicación pide confirmación.
- La aplicación elimina todas las criaturas activas.
- La aplicación conserva la configuración.
- La aplicación conserva usos diarios.
- La aplicación persiste el estado.

---

# CU-09 — Gestionar usos diarios

## Historia de usuario

Como usuario,  
quiero ver y ajustar los usos diarios de invocación,  
para controlar cuántos usos me quedan durante la sesión.

## Precondiciones

- La aplicación está abierta.
- Existe configuración de usos máximos.
- El usuario está en la pantalla principal.

## Disparador

El usuario consulta o modifica el contador de usos diarios.

## Flujo principal

1. La aplicación muestra usos restantes y usos máximos.
2. El usuario puede sumar usos manualmente.
3. El usuario puede restar usos manualmente.
4. El usuario puede resetear usos diarios.
5. Cuando invoca una criatura, la aplicación descuenta automáticamente 1 uso si `remaining > 0`.
6. La aplicación guarda el estado.

## Flujos alternativos

### A1 — Sumar usos estando ya en máximo

1. El contador está en `remaining = maximum`.
2. El usuario pulsa sumar usos.
3. La aplicación mantiene `remaining = maximum`.
4. La aplicación no persiste `remaining > maximum`.

### A2 — Restar usos estando en 0

1. El contador está en `remaining = 0`.
2. El usuario pulsa restar usos.
3. La aplicación mantiene `remaining = 0`.
4. La aplicación no persiste valores negativos.

### A3 — Resetear usos diarios

1. El usuario pulsa resetear usos diarios.
2. La aplicación establece `remaining = maximum`.
3. La aplicación guarda el estado.

### A4 — Invocar con 0 usos restantes

1. El contador está en `remaining = 0`.
2. El usuario invoca una criatura.
3. La aplicación no bloquea rígidamente la invocación en el MVP.
4. La aplicación mantiene `remaining = 0`.
5. La aplicación no persiste valores negativos.

### A5 — Reducir usos máximos por debajo de los restantes

1. El usuario actualiza `maximum` a un valor menor que el `remaining` actual.
2. La aplicación acepta el nuevo máximo si es válido.
3. La aplicación ajusta `remaining = min(remaining, maximum)`.
4. La aplicación guarda un estado coherente.

## Resultado esperado

El contador de usos diarios refleja el valor actual y puede corregirse manualmente sin salir de los límites `maximum >= 0` y `0 <= remaining <= maximum`.

## Criterios de aceptación

- La pantalla muestra usos restantes.
- La pantalla muestra usos máximos.
- El usuario puede sumar usos.
- El usuario puede restar usos.
- El usuario puede resetear usos.
- Invocar descuenta 1 uso automáticamente solo si `remaining > 0`.
- Invocar con `remaining = 0` no deja el contador en negativo.
- Sumar usos no permite superar `maximum`.
- Restar usos no permite bajar de 0.
- Resetear deja `remaining = maximum`.
- La aplicación persiste el contador.

---

# CU-10 — Recuperar estado tras recargar

## Historia de usuario

Como usuario,  
quiero que el estado de la aplicación se conserve si recargo la página o bloqueo la tablet,  
para no perder las invocaciones activas durante la partida.

## Precondiciones

- La aplicación ha sido usada previamente.
- Existe estado persistido.

## Disparador

Ocurre una de estas acciones:

- el usuario recarga la página;
- el usuario cierra y vuelve a abrir el navegador;
- la tablet bloquea la pantalla;
- la aplicación se vuelve a cargar.

## Flujo principal

1. La aplicación arranca.
2. La aplicación busca estado persistido.
3. Si existe estado, lo restaura.
4. La pantalla principal muestra las invocaciones activas.
5. Se restauran PG actuales.
6. Se restauran usos diarios.
7. Se restauran configuración relevante.
8. Se restauran últimas usadas y más usadas.

## Resultado esperado

El usuario puede continuar usando la aplicación sin reconstruir manualmente el combate.

## Criterios de aceptación

- Las invocaciones activas se conservan.
- Los grupos activos se conservan.
- Las cards individuales se conservan.
- Los PG actuales se conservan.
- Los usos diarios se conservan.
- La configuración se conserva.
- Últimas usadas y más usadas se conservan.

---

# CU-11 — Ver resultado más reciente de tirada

## Historia de usuario

Como usuario,  
quiero ver el resultado más reciente de una tirada,  
para consultarlo rápidamente mientras resuelvo la acción en mesa.

## Precondiciones

- El usuario ha realizado al menos una tirada.
- La aplicación está en la pantalla principal.

## Disparador

El usuario realiza una tirada de ataque, daño, salvaciones o cantidad de criaturas invocadas.

## Flujo principal

1. La aplicación realiza la tirada.
2. La aplicación muestra el resultado.
3. El resultado queda visible como resultado actual o más reciente.
4. Si el usuario realiza otra tirada, el resultado anterior puede ser sustituido.

## Resultado esperado

El usuario ve el resultado necesario para resolver la acción actual.

## Criterios de aceptación

- La aplicación muestra el resultado de la última tirada.
- No es obligatorio guardar historial completo en el MVP.
- El usuario puede repetir una tirada pulsando de nuevo el botón correspondiente.

---

# CU-12 — Marcar criatura como caída por PG

## Historia de usuario

Como usuario,  
quiero que una criatura se marque visualmente cuando queda a 0 PG o menos,  
para identificar rápidamente qué criaturas probablemente están fuera de combate.

## Precondiciones

- Hay al menos una criatura activa.
- La criatura recibe daño o tiene sus PG modificados.

## Disparador

Los PG actuales de una criatura pasan a ser 0 o menos.

## Flujo principal

1. La aplicación actualiza los PG de la criatura.
2. La aplicación detecta que los PG son 0 o menos.
3. La aplicación recalcula el estado de instancia como `DOWN`.
4. La aplicación marca la card como caída.
5. La criatura permanece en pantalla.
6. El usuario puede seguir dañándola, curándola o eliminándola.
7. La aplicación guarda el estado.

## Resultado esperado

La criatura se identifica visualmente como caída (`DOWN`), pero no desaparece.

## Criterios de aceptación

- La aplicación marca criaturas con PG a 0 o menos como `DOWN`.
- La aplicación no elimina automáticamente criaturas caídas.
- La aplicación permite seguir interactuando con criaturas caídas.
- La marca desaparece si la criatura vuelve a tener más de 0 PG.
- Al recuperarse por encima de 0 PG, la criatura pasa a `HEALTHY` o `DAMAGED` según sus PG actuales.
- La aplicación persiste el estado.

---

# CU-13 — Limpiar resultado de tirada

## Historia de usuario

Como usuario,  
quiero limpiar el resultado visible de una tirada,  
para despejar la pantalla sin borrar invocaciones ni alterar el estado del combate.

## Precondiciones

- Existe un resultado de tirada visible en pantalla.
- La aplicación está en la pantalla principal.

## Disparador

El usuario pulsa el botón o acción:

```text
Limpiar resultado
```

## Flujo principal

1. La aplicación solicita a la API limpiar el resultado actual de tirada.
2. La API pone `lastRollResult` a `null`.
3. La API persiste el estado actualizado.
4. La pantalla deja de mostrar el resultado de tirada.
5. Las invocaciones activas permanecen sin cambios.

## Resultado esperado

El resultado visible desaparece de la pantalla.

No se modifican:

- grupos de criaturas activas;
- instancias individuales;
- PG actuales;
- usos diarios;
- configuración;
- últimas usadas;
- más usadas.

## Criterios de aceptación

- El usuario puede limpiar el resultado visible sin limpiar invocaciones.
- `lastRollResult` queda vacío.
- La pantalla deja de mostrar el resultado anterior.
- No cambian criaturas activas, PG ni usos diarios.
- No se crea historial de tiradas.
- La aplicación persiste el estado.

---

# Casos de uso fuera del MVP

Los siguientes casos no forman parte del MVP:

- elegir invocación mediante diagrama de preguntas;
- aplicar modificadores temporales;
- elegir objetivo enemigo y comprobar impacto contra CA;
- aplicar daño automáticamente a enemigos;
- gestionar estados completos;
- gestionar iniciativa;
- gestionar posicionamiento;
- integrar con Fantasy Grounds;
- gestionar usuarios o login;
- gestionar duración exacta en asaltos;
- automatizar hechizos de criaturas invocadas;
- automatizar auras;
- resolver ataques especiales contextuales.
