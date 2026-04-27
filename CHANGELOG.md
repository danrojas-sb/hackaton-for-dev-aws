# Changelog

Todos los cambios notables de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y este proyecto adhiere a [Versionamiento Semántico](https://semver.org/lang/es/).

## [No publicado]

### Agregado
- Se creó estructura del repositorio con carpetas `boveda/` y `proyecto/`
- Se creó documentación del proyecto en Obsidian (Contexto, Sprint-1, Alcance, Estrategia de Trabajo)
- Se creó Docker Compose con MySQL 8.0 y esquemas `centralizador` + `db_procesos_masivos`
- Se creó script SQL de inicialización con tablas `facturas` y `facturas_contabilizadas`
- Se creó carpeta `.kiro/` con sprint-1 para tracking de tareas
- Se creó carpeta `postmanCollections/` para colecciones de Postman
- Se inicializó proyecto Spring Boot 3.4.5 con Maven Wrapper en `proyecto/backend/`
- Se creó entidad JPA `Factura` mapeada a tabla `centralizador.facturas` con 18 columnas
- Se creó enum `EstadoFactura` (PENDIENTE, PROCESO, TERMINADO, ERROR)
- Se creó repositorio `FacturaRepository` (JpaRepository)
- Se configuró `application.yml` con MySQL localhost:3307/centralizador y ddl-auto=validate
- Se creó DTO `FacturaRequest` con validaciones Jakarta (@NotBlank, @NotNull, @Positive, @Size, @Email)
- Se creó DTO `FacturaResponse` con todos los campos + id + estado + fechaCreacion
- Se creó `FacturaService.crearFacturasEnLote()` con mapeo a entidad, estado PENDIENTE y saveAll
- Se creó `FacturaController` con endpoint POST /api/facturas/bulk y manejo de errores de validación
- Se creó `CorsConfig` permitiendo peticiones desde localhost:4200
- Se crearon tests unitarios para `FacturaService` (estado PENDIENTE, lista vacía)
- Se crearon tests de integración para `FacturaController` (200 con facturas, 400 con errores)

### Corregido
- Se corrigió bug de delayed expansion en `mvnw.cmd` que impedía ejecutar Maven Wrapper en Windows
