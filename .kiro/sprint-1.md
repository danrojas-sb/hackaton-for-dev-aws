# Sprint 1 - Iteración Hackathon

## Objetivo del Sprint

Construir el microservicio completo de cargue de facturas packs con persistencia, control de estados, integración SQS y dashboard Angular funcional.

## Duración

Hackathon - Iteración única

## Historias de Usuario

### HU-01: Carga masiva de facturas
**Como** operador del Centralizador I&E
**Quiero** cargar un lote de facturas a través del API
**Para** que se persistan con estado Pendiente y se inicie su procesamiento

**Criterios de aceptación:**
- El endpoint POST /api/facturas/bulk recibe una lista de facturas en JSON
- Cada factura se persiste en MySQL con estado PENDIENTE
- Se validan los campos obligatorios (numeroDocumento, nombres, valorPack)
- Se retorna la lista de facturas creadas con sus IDs y estados

### HU-02: Consulta de facturas y estados
**Como** operador del Centralizador I&E
**Quiero** consultar las facturas cargadas y ver un resumen de estados
**Para** tener visibilidad del progreso del procesamiento

**Criterios de aceptación:**
- GET /api/facturas retorna lista paginada de facturas
- GET /api/facturas/estados/resumen retorna conteo por estado
- Los datos alimentan la tabla y el diagrama de torta en el frontend

### HU-03: Envío a SQS y actualización de estados
**Como** sistema
**Quiero** enviar las facturas a la cola SQS y actualizar estados según respuesta
**Para** orquestar la contabilización en `db_procesos_masivos` de forma asíncrona (bosquejo para TRONADOR en sprint futuro)

**Criterios de aceptación:**
- Al crear facturas, se envían a la cola SQS
- El estado cambia de PENDIENTE a PROCESO al enviar
- La Lambda contabiliza en la tabla `facturas_contabilizadas` del esquema `db_procesos_masivos`
- Al recibir respuesta de la Lambda, el estado cambia a TERMINADO o ERROR
- Las facturas en ERROR pueden reintentarse

### HU-04: Dashboard Angular
**Como** operador del Centralizador I&E
**Quiero** ver un dashboard con tabla de facturas y diagrama de torta
**Para** monitorear el estado general de las facturas cargadas

**Criterios de aceptación:**
- Tabla con columnas: documento, nombres, apellidos, municipio, valorPack, valorPackIva, estado, fechaPago
- Diagrama de torta con porcentaje por estado (colores: Pendiente=amarillo, Proceso=azul, Terminado=verde, Error=rojo)
- Funcionalidad de carga de lote desde el frontend

## Tareas del Sprint

| # | Tarea | HU | Asignado | Estimación | Estado |
|---|-------|----|----------|------------|--------|
| 1 | Clonar repo, crear estructura y documentación | - | Equipo | S | ✅ Completado |
| 2 | Inicializar proyecto Spring Boot + MySQL + entidad Factura | HU-01 | Persona 1 | S | ⬜ Pendiente |
| 3 | Endpoint POST /api/facturas/bulk con validaciones | HU-01 | Persona 1 | M | ⬜ Pendiente |
| 4 | Endpoints GET /api/facturas y GET /api/facturas/estados/resumen | HU-02 | Persona 1 | S | ⬜ Pendiente |
| 5 | Integración SQS - Productor y Listener | HU-03 | Persona 2 | M | ⬜ Pendiente |
| 6 | Frontend Angular - Proyecto base + tabla de facturas | HU-04 | Persona 3 | M | ⬜ Pendiente |
| 7 | Frontend Angular - Diagrama de torta + carga + dashboard | HU-04 | Persona 3 | M | ⬜ Pendiente |

*Estimación: S=Small, M=Medium*

## Definition of Done

- [ ] Código compilado sin errores
- [ ] Tests unitarios y de integración pasando
- [ ] Endpoints probados con Postman/curl
- [ ] Frontend conectado al backend y funcional
- [ ] Documentación actualizada
