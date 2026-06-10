# Ayuda rápida

Este proyecto es una aplicación local de **Pathfinder 1e** para gestionar invocaciones en mesa.

## Dónde mirar primero

- [README](README.md)
- [Visión del producto](docs/01-vision-producto.md)
- [Alcance funcional](docs/02-alcance-funcional.md)
- [Pantallas](docs/03-pantalla-principal.md)
- [API REST](docs/10-api-rest.md)
- [Modelo de dominio](docs/13-modelo-dominio.md)

## Comandos habituales

### Backend

```powershell
./mvnw spring-boot:run
```

### Tests

```powershell
./mvnw test
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

### Build de producción

```powershell
cd frontend
npm install
npm run build
Copy-Item -Recurse -Force .\dist\* ..\src\main\resources\static\
cd ..
./mvnw clean package
```
