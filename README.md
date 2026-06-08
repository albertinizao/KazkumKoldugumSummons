# Kazkum Koldugum Summons

Aplicación local para gestionar invocaciones de Pathfinder 1e.

## Requisitos

- Java 25
- Node.js 22 o superior
- npm

El proyecto usa el wrapper de Maven, así que no necesitas instalar Maven globalmente.

## Estructura

- `src/` — backend Spring Boot
- `frontend/` — frontend Vue 3 + Vite
- `data/` — base de datos local H2 en archivo

## Compilación del backend

Desde la raíz del repositorio:

```powershell
./mvnw test
```

Ese comando compila el backend y ejecuta la batería de tests.

Si quieres generar el JAR final:

```powershell
./mvnw clean package
```

El artefacto queda en:

```text
target/kazkum-koldugum-summons-0.0.1-SNAPSHOT.jar
```

## Compilación del frontend

Desde `frontend/`:

```powershell
npm install
npm run build
```

El resultado de producción se genera en:

```text
frontend/dist
```

## Desarrollo en local

### Backend

```powershell
./mvnw spring-boot:run
```

Arranca en:

```text
http://localhost:8080
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

Vite arranca en:

```text
http://localhost:5173
```

El frontend de desarrollo usa proxy hacia la API en `http://localhost:8080`.

## Despliegue local completo

El backend sirve la interfaz estática desde `src/main/resources/static`, así que el despliegue local completo consiste en:

1. Compilar el frontend.
2. Copiar el contenido de `frontend/dist` a `src/main/resources/static`.
3. Compilar el backend.
4. Ejecutar el JAR.

### Pasos exactos

```powershell
cd frontend
npm install
npm run build
```

```powershell
Copy-Item -Recurse -Force .\dist\* ..\src\main\resources\static\
```

```powershell
cd ..
./mvnw clean package
```

```powershell
java -jar .\target\kazkum-koldugum-summons-0.0.1-SNAPSHOT.jar
```

La aplicación quedará disponible en:

```text
http://localhost:8080
```

## Base de datos local

El backend persiste el estado en un archivo H2 local dentro de `data/`.

Si quieres reiniciar el estado completo, elimina ese archivo con la aplicación cerrada.

## Nota operativa

Si cambias el frontend, vuelve a ejecutar `npm run build` y copia de nuevo `frontend/dist` a `src/main/resources/static` antes de empaquetar el backend.
