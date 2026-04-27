# Requirements: HU-02 - Consulta de facturas y estados

## User Story
**Como** operador del Centralizador I&E
**Quiero** consultar las facturas cargadas y ver un resumen de estados
**Para** tener visibilidad del progreso del procesamiento

## Acceptance Criteria

### AC-1: Lista paginada de facturas
- **Given** facturas existentes en la BD
- **When** se envía GET a /api/facturas?page=0&size=10
- **Then** responde 200 con Page<FacturaResponse> incluyendo content, totalElements, totalPages

### AC-2: Resumen de estados para diagrama de torta
- **Given** facturas con distintos estados en la BD
- **When** se envía GET a /api/facturas/estados/resumen
- **Then** responde 200 con {PENDIENTE: N, PROCESO: N, TERMINADO: N, ERROR: N}

### AC-3: Resumen incluye todos los estados aunque tengan conteo 0
- **Given** solo facturas en estado PENDIENTE
- **When** se consulta el resumen
- **Then** PROCESO, TERMINADO y ERROR aparecen con valor 0

### AC-4: Paginación con parámetros por defecto
- **Given** facturas existentes
- **When** se envía GET a /api/facturas sin parámetros
- **Then** responde con page=0, size=20 por defecto
