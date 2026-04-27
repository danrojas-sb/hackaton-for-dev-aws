# Documento de Diseño: HU-04 — Dashboard Angular

## Arquitectura

```
proyecto/frontend/ (Angular 21)
  src/app/
    models/
      factura.model.ts          (interfaces Factura, PageResponse, EstadoResumen)
    services/
      factura.service.ts        (HttpClient -> API REST)
    components/
      factura-table/            (tabla paginada con badges de estado)
      estados-chart/            (diagrama de torta con ng2-charts)
      carga-facturas/           (input CSV, parseo, POST bulk)
      dashboard/                (integra los 3 componentes)
    app.component.ts            (router-outlet)
  proxy.conf.json               (/api -> localhost:8080)
  angular.json
```

## Componentes

### 1. factura.model.ts

Interfaces TypeScript que modelan las respuestas del API:
- Factura: todos los campos del FacturaResponse del backend
- PageResponse<T>: content, totalElements, totalPages, number, size
- EstadoResumen: mapa string -> number para el conteo por estado

### 2. factura.service.ts

Servicio Angular con inject(HttpClient):
- listar(page, size) -> GET /api/facturas?page=N&size=N -> Observable<PageResponse<Factura>>
- resumenEstados() -> GET /api/facturas/estados/resumen -> Observable<EstadoResumen>
- crearEnLote(facturas) -> POST /api/facturas/bulk -> Observable<Factura[]>

### 3. factura-table (standalone component, OnPush)

- Input: datos de Page<Factura>
- Tabla HTML con columnas: numeroDocumento, nombres, apellidos, municipio, valorPack, valorPackIva, estado, fechaPago
- Badges de color por estado usando [class] binding
- Paginador con botones prev/next + numero de pagina
- Emite evento (pageChange) cuando cambia de pagina

### 4. estados-chart (standalone component, OnPush)

- Input: datos de EstadoResumen
- Usa ng2-charts (Chart.js) con tipo pie
- Colores fijos: PENDIENTE=#FFC107, PROCESO=#2196F3, TERMINADO=#4CAF50, ERROR=#F44336
- Muestra labels con porcentaje

### 5. carga-facturas (standalone component, OnPush)

- Input file que acepta .csv
- Parsea CSV a JSON (split por comas, headers de primera linea)
- Boton Cargar que llama facturaService.crearEnLote()
- Muestra resultado: exito (N facturas creadas) o errores de validacion
- Emite evento (cargaExitosa) para refrescar dashboard

### 6. dashboard (standalone component, OnPush)

- Orquesta los 3 componentes
- Llama facturaService.listar() y facturaService.resumenEstados() al iniciar
- Escucha (cargaExitosa) para refrescar datos
- Layout: carga arriba, torta y tabla lado a lado abajo

## Proxy Config (proxy.conf.json)

Redirige /api a localhost:8080 durante desarrollo local.

## Dependencias

- ng2-charts (^7.0.0)
- chart.js (^4.4.0)

## Convenciones Angular (segun steerings)

- Standalone components (sin NgModules)
- ChangeDetectionStrategy.OnPush
- inject() en lugar de constructor injection
- Native control flow (@if, @for)
- Signals para estado reactivo
- input() y output() en lugar de decoradores
