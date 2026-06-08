# Criterios de aceptación

Este documento define los criterios de aceptación del MVP.

Sirve para validar si la aplicación está lista para probarse en mesa. Los criterios están escritos de forma funcional, orientados a comprobar comportamientos observables desde la interfaz.

---

# Principio general de aceptación

La aplicación se considera aceptable para el MVP si permite gestionar invocaciones activas de forma rápida durante un combate, sin consultar constantemente documentos externos y sin automatizar reglas fuera del alcance definido.

La validación debe hacerse pensando en uso real desde una tablet Android, con el usuario en mesa y con necesidad de resolver acciones en pocos toques.

---

# 1. Pantalla principal

## CA-01 — Visualización en tablet Android

**Dado** que el usuario abre la aplicación desde una tablet Android,  
**cuando** accede a la pantalla principal,  
**entonces** la pantalla debe verse correctamente y permitir el uso normal de la aplicación.

Debe cumplirse:

- La pantalla debe ser usable en vertical.
- La pantalla debe verse correctamente en horizontal.
- Los botones principales deben ser pulsables cómodamente.
- La información principal no debe quedar cortada.
- No debe depender de hover ni de interacciones propias de escritorio.
- El modo oscuro debe estar disponible desde el inicio.

## CA-02 — Cabecera principal

**Dado** que el usuario está en la pantalla principal,  
**cuando** mira la parte superior de la pantalla,  
**entonces** debe poder ver las acciones globales principales.

Debe mostrar:

- botón `Invocar`;
- botón `Limpiar invocaciones`;
- botones globales `Atacar con todas` y `Tirar TS con todas`;
- contador de usos diarios;

## CA-03 — Agrupación por tipo de criatura

**Dado** que hay criaturas activas,  
**cuando** se muestra la pantalla principal,  
**entonces** las criaturas deben aparecer agrupadas por tipo final de criatura.

Deben agruparse juntas si coinciden en:

- criatura base;
- plantilla aplicada;
- reglas fijas aplicadas;
- ficha final resultante.

No deben agruparse juntas si tienen distinta plantilla o distinta ficha final.

## CA-04 — Información visible por grupo

**Dado** que existe un grupo de criaturas activas,  
**cuando** el usuario lo consulta en pantalla,  
**entonces** debe ver una ficha resumida suficiente para mesa.

Debe mostrar, como mínimo:

- nombre final;
- alineamiento;
- tamaño;
- tipo con subtipo visible cuando corresponda;
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
- número de criaturas activas del grupo.

No debe mostrar notas tácticas en la ficha resumida del MVP.

## CA-05 — Cards individuales

**Dado** que un grupo tiene una o más criaturas activas,  
**cuando** se muestra el grupo,  
**entonces** cada criatura concreta debe aparecer como una instancia individual.

Cada card debe mostrar:

- nombre visible de la criatura;
- PG máximos;
- PG actuales;
- botones rápidos `-10`, `-5`, `-1`, `+1`, `+5`, `+10`;
- campo libre para introducir una cantidad con signo;
- botón `Eliminar`.

## CA-06 — Distinguir criaturas sanas, dañadas y caídas

**Dado** que hay criaturas con distintos valores de PG,  
**cuando** se muestran sus cards,  
**entonces** el usuario debe poder distinguir rápidamente su estado.

Debe distinguirse:

- criatura sana (`HEALTHY`): PG actuales igual a PG máximos;
- criatura dañada (`DAMAGED`): PG actuales mayor que 0 y menor que PG máximos;
- criatura caída (`DOWN`): PG actuales igual o menor que 0.

Estos son los únicos estados visibles/API de una instancia en el MVP.

La aplicación no debe borrar automáticamente criaturas caídas.

## CA-07 — Acciones globales de grupo

**Dado** que existe un grupo de criaturas activas,  
**cuando** el usuario consulta el grupo,  
**entonces** debe tener disponibles las acciones globales del grupo.

Debe mostrar:

- `Atacar`;
- `Salvaciones`;
- `Expandir ficha`.

No debe haber botones separados para Fortaleza, Reflejos y Voluntad en el MVP.

---

# 2. Invocación

## CA-08 — Abrir modal de invocación

**Dado** que el usuario está en la pantalla principal,  
**cuando** pulsa `Invocar`,  
**entonces** debe abrirse un modal o panel de invocación.

El modal debe permitir seleccionar criatura mediante:

- últimas usadas;
- más usadas;
- búsqueda manual.

El diagrama de preguntas queda fuera del MVP.

## CA-09 — Selección manual con plantilla

**Dado** que el usuario selecciona una criatura mediante búsqueda manual,  
**cuando** esa criatura permite varias plantillas,  
**entonces** la aplicación debe pedir qué plantilla aplicar.

Plantillas previstas:

- `Chthonic`;
- `Fiery`;
- `Celestial`;
- `Entropic`;
- `Resolute`.

## CA-10 — Selección desde últimas o más usadas

**Dado** que el usuario selecciona una criatura desde últimas usadas o más usadas,  
**cuando** esa entrada ya representa una combinación criatura + plantilla,  
**entonces** la aplicación debe invocarla directamente con esa plantilla.

No debe volver a preguntar plantilla en ese caso.

## CA-11 — Cálculo automático de cantidad

**Dado** que el usuario confirma una invocación,  
**cuando** la criatura tiene un nivel de `Summon Monster` inferior al máximo actual,  
**entonces** la aplicación debe calcular automáticamente cuántas criaturas aparecen.

Regla funcional:

| Relación con nivel máximo | Cantidad |
|---|---:|
| Nivel máximo | 1 |
| Nivel máximo - 1 | `1d3 + 1` |
| Nivel máximo - 2 | `1d4 + 2` |

El usuario no debe introducir manualmente la cantidad en el MVP.

## CA-12 — Crear instancias separadas

**Dado** que una invocación genera varias criaturas iguales,  
**cuando** la aplicación las añade al combate,  
**entonces** debe crear una instancia separada para cada criatura.

Ejemplo:

```text
Invocar Fiery Badger
Cantidad: 3

Resultado:
- Fiery Badger 1
- Fiery Badger 2
- Fiery Badger 3
```

## CA-13 — Compartir ficha de tipo y mantener PG independientes

**Dado** que hay tres criaturas iguales en un grupo,  
**cuando** se muestran en pantalla,  
**entonces** deben compartir la misma ficha de tipo, pero tener PG independientes.

Ejemplo:

```text
Fiery Badger x3

Fiery Badger 1 — PG 15 / 15
Fiery Badger 2 — PG 7 / 15
Fiery Badger 3 — PG -2 / 15
```

## CA-14 — Eliminar una instancia sin afectar a las demás

**Dado** que hay varias criaturas en el mismo grupo,  
**cuando** el usuario elimina una de ellas,  
**entonces** las demás deben permanecer en pantalla con sus PG actuales.

Si el grupo se queda sin criaturas, debe desaparecer el grupo completo.

## CA-15 — Aplicación de reglas fijas al invocar

**Dado** que el usuario invoca una criatura,  
**cuando** la aplicación genera la ficha final,  
**entonces** debe aplicar las reglas fijas del personaje que correspondan.

Debe aplicar:

- `Augment Summoning`;
- plantilla de `Versatile Summon Monster`, si procede;
- `Deep Guardian`, si procede;
- cantidad adicional de `Superior Summoning`, si se tira cantidad.

## CA-16 — Descuento de usos diarios

**Dado** que el usuario confirma una invocación,  
**cuando** la criatura se añade a la pantalla principal,  
**entonces** la aplicación debe descontar automáticamente 1 uso diario.

El contador debe poder corregirse manualmente con sumar, restar o resetear usos.

---

# 3. Daño, curación y eliminación


## CA-17 — Usos diarios dentro de límites

**Dado** que la aplicación muestra o persiste usos diarios,  
**cuando** se consulta el contador,  
**entonces** debe cumplir siempre `maximum >= 0` y `0 <= remaining <= maximum`.

No debe persistirse ningún estado con `remaining < 0` ni con `remaining > maximum`.

## CA-18 — Invocar con 0 usos restantes

**Dado** que los usos diarios restantes están a 0,  
**cuando** el usuario invoca una criatura válida,  
**entonces** el MVP puede permitir la invocación, pero el contador debe permanecer en 0.

No debe generarse `remaining = -1` ni ningún valor negativo.

## CA-19 — Sumar y restar usos sin salir de límites

**Dado** que el usuario corrige manualmente los usos diarios,  
**cuando** suma usos estando en el máximo o resta usos estando en 0,  
**entonces** el contador debe mantenerse dentro de sus límites.

Debe cumplirse:

- sumar usos no puede dejar `remaining > maximum`;
- restar usos no puede dejar `remaining < 0`.

## CA-20 — Resetear usos diarios

**Dado** que el usuario pulsa resetear usos diarios,  
**cuando** la aplicación completa la acción,  
**entonces** debe dejar `remaining = maximum`.

El estado resultante debe persistirse.

## CA-21 — Aplicar daño o curación en menos de 3 pulsaciones

**Dado** que el usuario quiere ajustar los PG de una criatura concreta,  
**cuando** usa un valor rápido o introduce una cantidad con signo,  
**entonces** debe poder aplicar el cambio en menos de 3 pulsaciones desde la pantalla principal.

Flujo esperado:

1. Pulsar una cantidad rápida o escribir un valor.
2. Confirmar solo si la interfaz lo requiere.

Si se usan botones rápidos con aplicación inmediata, puede resolverse en 1 o 2 pulsaciones.

## CA-22 — Daño individual

**Dado** que el usuario aplica daño a una criatura,  
**cuando** confirma el daño,  
**entonces** solo deben modificarse los PG de esa criatura concreta.

No debe cambiar los PG de otras criaturas del mismo grupo.


## CA-23 — Rechazar daño inválido

**Dado** que el usuario introduce un valor vacío, `0`, negativo, decimal o no numérico,  
**cuando** intenta aplicar daño a una criatura,  
**entonces** la aplicación debe mostrar un error y no modificar los PG.

También debe cumplirse:

- No cambia el estado de la instancia.
- No se persiste ningún cambio.
- La API debe devolver `INVALID_DAMAGE_AMOUNT`.
- El endpoint `/damage` determina la resta; el cliente no debe enviar cantidades negativas.

## CA-24 — Ajuste de PG con defensas visibles

**Dado** que la criatura tiene RD, resistencia, inmunidad o vulnerabilidad,  
**cuando** el usuario ajusta sus PG,  
**entonces** la interfaz debe mostrar esas defensas como referencia.

La aplicación no debe aplicar automáticamente estas defensas.

El usuario introduce el daño final.

## CA-25 — Marcar caída a 0 PG o menos

**Dado** que una criatura recibe daño,  
**cuando** sus PG actuales quedan a 0 o menos,  
**entonces** la aplicación debe marcarla visualmente como caída.

La criatura debe seguir visible.

La criatura debe seguir permitiendo:

- ajustar PG;
- eliminar.

## CA-26 — Curación individual

**Dado** que el usuario cura una criatura,  
**cuando** confirma la curación,  
**entonces** solo deben modificarse los PG de esa criatura concreta.

La curación no debe superar los PG máximos en el MVP.

## CA-27 — Recuperar criatura caída

**Dado** que una criatura caída recibe curación,  
**cuando** sus PG actuales pasan a ser mayores que 0,  
**entonces** debe dejar de mostrarse como caída.

El estado resultante debe recalcularse según sus PG:

- `HEALTHY` si los PG actuales son iguales a los PG máximos;
- `DAMAGED` si los PG actuales son mayores que 0 y menores que los PG máximos.


## CA-28 — Rechazar curación inválida

**Dado** que el usuario introduce un valor vacío, `0`, negativo, decimal o no numérico,  
**cuando** intenta ajustar los PG de una criatura,  
**entonces** la aplicación debe mostrar un error y no modificar los PG.

También debe cumplirse:

- No cambia el estado de la instancia.
- No se persiste ningún cambio.
- La API debe devolver `INVALID_HEAL_AMOUNT`.
- El endpoint `/heal` determina la suma; el cliente no debe enviar cantidades negativas.

## CA-29 — Eliminar criatura individual

**Dado** que el usuario pulsa `Eliminar` en una criatura,  
**cuando** confirma la acción,  
**entonces** esa criatura debe desaparecer de la pantalla.

La eliminación no debe representarse como un estado visible/API ni persistido. La instancia eliminada se borra del grupo y, si el grupo queda vacío, el grupo desaparece.

La aplicación no debe eliminar criaturas automáticamente por estar a 0 PG o menos.

---

# 4. Tiradas de ataque

## CA-30 — Tirar ataques de todo un grupo con 1 pulsación

**Dado** que existe un grupo de criaturas activas,  
**cuando** el usuario pulsa `Atacar`,  
**entonces** la aplicación debe tirar todos los ataques de todas las criaturas de ese grupo con una sola pulsación.

## CA-31 — Resultado agrupado por criatura

**Dado** que se tira ataque con un grupo,  
**cuando** se muestra el resultado,  
**entonces** debe estar agrupado por criatura individual.

Ejemplo:

```text
Fiery Badger 1
Bite: d20 14 + 4 = 18
Daño si impacta: 1d3+3 = 5 piercing + 1 fire

Fiery Badger 2
Bite: d20 8 + 4 = 12
Daño si impacta: 1d3+3 = 4 piercing + 1 fire
```

## CA-32 — Mostrar dado, modificador y total

**Dado** que la aplicación muestra una tirada de ataque,  
**cuando** el usuario consulta el resultado,  
**entonces** debe ver dado, modificador y total.

Formato aceptado:

```text
d20 14 + 4 = 18
```

## CA-33 — Daño separado del ataque

**Dado** que la aplicación tira ataque y daño,  
**cuando** muestra el resultado,  
**entonces** el daño debe aparecer separado de la tirada de ataque.

Debe mostrarse como:

```text
Daño si impacta
```

## CA-34 — Daño separado por tipo

**Dado** que un ataque tiene varios tipos de daño,  
**cuando** se muestra el resultado,  
**entonces** debe mostrar cada componente por separado.

Ejemplo:

```text
Daño si impacta: 1d3+3 = 5 piercing + 1 fire
```

No basta con mostrar solo el total.

## CA-35 — Ataques con cantidad

**Dado** que un ataque tiene `cantidad` mayor que 1,  
**cuando** se tira el ataque,  
**entonces** la aplicación debe tirar cada repetición por separado.

Ejemplo:

```text
Claw 1
Claw 2
```

## CA-36 — Críticos

**Dado** que una tirada natural amenaza crítico,  
**cuando** la aplicación muestra el resultado,  
**entonces** debe mostrar:

- tirada de amenaza;
- tirada de confirmación;
- daño normal;
- daño crítico.

El daño normal debe estar visible aunque haya crítico.

## CA-37 — No decidir impacto

**Dado** que la aplicación muestra una tirada de ataque,  
**cuando** el usuario consulta el resultado,  
**entonces** la aplicación no debe indicar automáticamente si el ataque impacta o falla.

El usuario resuelve esa parte en mesa.

---

# 5. Tiradas de salvación

## CA-38 — Salvaciones de todo el grupo

**Dado** que existe un grupo de criaturas activas,  
**cuando** el usuario pulsa `Salvaciones`,  
**entonces** la aplicación debe tirar Fortaleza, Reflejos y Voluntad de todas las criaturas del grupo.

## CA-39 — Mostrar dado, modificador y total en TS

**Dado** que la aplicación muestra una tirada de salvación,  
**cuando** el usuario consulta el resultado,  
**entonces** debe ver dado, modificador y total.

Formato aceptado:

```text
Fortaleza: d20 12 + 8 = 20
```

## CA-40 — No resolver consecuencias

**Dado** que la aplicación muestra una tirada de salvación,  
**cuando** el usuario consulta el resultado,  
**entonces** la aplicación no debe resolver estados, daño recibido ni efectos de éxito o fallo.

---

# 6. Resultado de tiradas

## CA-41 — Visibilidad del resultado

**Dado** que el usuario realiza una tirada,  
**cuando** la aplicación muestra el resultado,  
**entonces** el resultado debe quedar visible en la modal/panel temporal hasta que ocurra una de estas acciones:

- se haga otra tirada;
- el usuario cierre la modal/panel;
- se limpien las invocaciones activas, si el diseño decide cerrar también el resultado.

## CA-42 — No historial obligatorio

**Dado** que el usuario realiza varias tiradas,  
**cuando** se muestra el resultado actual,  
**entonces** no es obligatorio conservar un historial completo en el MVP.

Solo debe estar disponible el resultado actual o más reciente.

## CA-43 — Repetir tirada

**Dado** que el usuario necesita repetir una tirada,  
**cuando** pulsa de nuevo el botón correspondiente,  
**entonces** la aplicación debe generar una nueva tirada y sustituir el resultado anterior.

---

# 7. Ficha expandida

## CA-44 — Expandir ficha

**Dado** que existe un grupo de criaturas activas,  
**cuando** el usuario pulsa `Expandir ficha`,  
**entonces** la aplicación debe mostrar la ficha completa de la criatura final.

## CA-45 — Ficha final transformada

**Dado** que se abre la ficha expandida,  
**cuando** el usuario la consulta,  
**entonces** debe ver la versión final transformada, no la ficha base sin modificar.

Debe incluir los cambios derivados de:

- `Augment Summoning`;
- plantilla aplicada, si procede;
- `Deep Guardian`, si procede.

Cuando se exponga mediante API o DTO, el campo técnico del texto completo debe llamarse `fullStatBlock`. No debe usarse `statBlockText`, `fichaCompleta` ni otro alias para la ficha expandida.

---

# 8. Persistencia

## CA-46 — Persistencia tras cambios

**Dado** que el usuario realiza una acción que cambia el estado,  
**cuando** la acción se completa,  
**entonces** la aplicación debe persistir el estado actualizado.

Acciones que deben persistirse:

- invocar;
- ajustar PG;
- eliminar;
- limpiar invocaciones;
- modificar usos diarios;
- resetear usos diarios.

## CA-47 — Recuperación tras recargar

**Dado** que existe estado persistido,  
**cuando** el usuario recarga la página o vuelve a abrir la aplicación,  
**entonces** debe recuperarse el estado anterior.

Debe recuperarse:

- grupos activos;
- criaturas individuales;
- PG actuales;
- usos diarios;
- configuración;
- últimas usadas;
- más usadas.

---

# 9. Limpieza de invocaciones

## CA-48 — Limpiar todas las invocaciones

**Dado** que el usuario pulsa `Limpiar invocaciones`,  
**cuando** confirma la acción,  
**entonces** deben eliminarse todos los grupos y criaturas activas.

Debe conservarse:

- configuración;
- usos diarios;
- últimas usadas;
- más usadas.

## CA-49 — Confirmación antes de limpiar

**Dado** que el usuario pulsa `Limpiar invocaciones`,  
**cuando** la aplicación va a eliminar criaturas activas,  
**entonces** debe pedir confirmación antes de borrar.

---

# 10. Fuera de aceptación del MVP

Estos comportamientos no son necesarios para aceptar el MVP:

- elegir invocación mediante diagrama de preguntas;
- gestionar duración exacta por asaltos;
- gestionar iniciativa;
- gestionar enemigos;
- gestionar mapa o posicionamiento;
- aplicar modificadores temporales;
- calcular carga, flanqueo, cobertura o inspiración de bardo;
- decidir si un ataque impacta;
- aplicar daño automáticamente a enemigos;
- resolver estados completos;
- automatizar auras;
- automatizar hechizos de criaturas invocadas;
- integración con Fantasy Grounds;
- login;
- multijugador;
- seguridad.
