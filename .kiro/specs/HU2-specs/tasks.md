# Tasks: HU-02 - Consulta de facturas y estados

## Task 4: Endpoints GET
- [x] Agregar GET /api/facturas en FacturaController (Pageable)
- [x] Agregar GET /api/facturas/estados/resumen en FacturaController
- [x] Verificar que FacturaService.listar() y resumenEstados() ya existen
- [x] Verificar que FacturaRepository.contarPorEstado() ya existe
- [x] Compilar y ejecutar tests

## Tests
- [x] Given facturas en BD, When GET /api/facturas, Then 200 con Page
- [x] Given facturas con distintos estados, When GET /api/facturas/estados/resumen, Then conteo correcto
- [x] Given sin facturas, When GET /api/facturas/estados/resumen, Then todos los estados en 0
