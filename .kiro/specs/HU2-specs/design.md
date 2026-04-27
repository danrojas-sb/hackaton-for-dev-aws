# Design: HU-02 - Consulta de facturas y estados

## Endpoints a agregar en FacturaController

### GET /api/facturas
- Parámetros: page (default 0), size (default 20), sort (default fechaCreacion,desc)
- Retorna: Page<FacturaResponse> (Spring Data Pageable)
- Usa: FacturaService.listar(Pageable)

### GET /api/facturas/estados/resumen
- Sin parámetros
- Retorna: Map<String, Long> con conteo por estado
- Usa: FacturaService.resumenEstados()
- Query: SELECT f.estado, COUNT(f) FROM Factura f GROUP BY f.estado

## Componentes a modificar
- FacturaController.java: agregar 2 endpoints GET
- FacturaService.java: agregar métodos listar() y resumenEstados()
- FacturaRepository.java: agregar query contarPorEstado()

## Nota
El FacturaService ya tiene los métodos listar() y resumenEstados() implementados desde HU-01.
El FacturaRepository ya tiene la query contarPorEstado().
Solo falta agregar los endpoints GET en el controller.
