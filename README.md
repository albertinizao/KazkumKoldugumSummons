# Kazkum Koldugum Summons

Aplicación web local para gestionar invocaciones de **Pathfinder 1e** en mesa.

## Estado actual

La versión actual del código expone cuatro vistas principales:

- **Combate** (`/`)
- **Invocaciones** (`/invocaciones`, alias `/invocacion`)
- **Catálogo** (`/catalogo`)
- **Configuración** (`/configuracion`)

El backend es **Java + Spring Boot** y el frontend es **Vue 3 + Vite**.
El estado persistente se guarda en **H2** en archivo local y el frontend mantiene una caché de respaldo en `localStorage`.

## Requisitos

- Java 25
- Node.js 22 o superior
- npm

El proyecto usa el wrapper de Maven, así que no hace falta instalar Maven globalmente.

## Estructura

- `src/` — backend Spring Boot
- `frontend/` — frontend Vue 3 + Vite
- `data/` — base de datos local H2 en archivo
- `docs/` — documentación funcional y técnica
- `openspec/` — specs en OpenSpec

## Ejecución local

### Backend

Desde la raíz del repositorio:

```powershell
./mvnw spring-boot:run
```

El backend queda disponible en:

```text
http://localhost:8080
```

### Frontend

Desde `frontend/`:

```powershell
npm install
npm run dev
```

El frontend de desarrollo queda disponible en:

```text
http://localhost:5173
```

En desarrollo, Vite proxy a la API del backend.

## Build de producción

### Frontend

```powershell
cd frontend
npm install
npm run build
```

El bundle queda en:

```text
frontend/dist
```

### Backend empaquetado

La app Spring Boot sirve la interfaz estática desde `src/main/resources/static`.

Pasos habituales:

```powershell
Copy-Item -Recurse -Force .\frontend\dist\* .\src\main\resources\static\
./mvnw clean package
java -jar .\target\kazkum-koldugum-summons-0.0.1-SNAPSHOT.jar
```

## Persistencia

- **H2**: `./data/summonsdb`
- **Spring Data JPA** para el estado del combate y la configuración
- **localStorage** en el frontend como caché de arranque, siempre subordinada a la API

## Documentación principal

- [Visión del producto](docs/01-vision-producto.md)
- [Alcance funcional actual](docs/02-alcance-funcional.md)
- [Pantallas](docs/03-pantalla-principal.md)
- [Catálogo](docs/04-catalogo-criaturas.md)
- [Reglas de tiradas](docs/05-reglas-tiradas.md)
- [Casos de uso](docs/07-casos-uso.md)
- [Criterios de aceptación](docs/08-criterios-aceptacion.md)
- [Glosario](docs/09-glosario.md)
- [API REST actual](docs/10-api-rest.md)
- [Modelo de dominio actual](docs/13-modelo-dominio.md)
