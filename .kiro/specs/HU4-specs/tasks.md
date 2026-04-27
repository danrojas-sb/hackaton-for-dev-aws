# Documento de Tareas: HU-04 — Dashboard Angular

## Task 8: Frontend Angular - Proyecto base + tabla de facturas

### Subtareas
- [ ] 8.1 Generar proyecto Angular en proyecto/frontend/
- [ ] 8.2 Instalar dependencias: ng2-charts, chart.js
- [ ] 8.3 Crear proxy.conf.json para redirigir /api a localhost:8080
- [ ] 8.4 Crear models/factura.model.ts con interfaces Factura, PageResponse, EstadoResumen
- [ ] 8.5 Crear services/factura.service.ts con metodos listar(), resumenEstados(), crearEnLote()
- [ ] 8.6 Crear components/factura-table/ con tabla HTML, badges de estado por color, paginacion
- [ ] 8.7 Verificar: ng serve muestra tabla con datos del backend

### Tests
- [ ] Test unitario factura.service.ts con HttpClientTestingModule
- [ ] Test unitario factura-table con datos mock

## Task 9: Frontend Angular - Diagrama de torta + carga + dashboard

### Subtareas
- [ ] 9.1 Crear components/estados-chart/ con diagrama de torta (ng2-charts, tipo pie)
- [ ] 9.2 Configurar colores: PENDIENTE=#FFC107, PROCESO=#2196F3, TERMINADO=#4CAF50, ERROR=#F44336
- [ ] 9.3 Crear components/carga-facturas/ con input CSV, parseo a JSON, POST bulk
- [ ] 9.4 Crear components/dashboard/ que integre tabla + torta + carga
- [ ] 9.5 Configurar auto-refresh de tabla y torta despues de carga exitosa
- [ ] 9.6 Configurar routing: / -> DashboardComponent
- [ ] 9.7 Verificar: dashboard completo funcional end-to-end

### Tests
- [ ] Test unitario estados-chart con datos mock
- [ ] Test unitario carga-facturas verificando parseo CSV
- [ ] Test unitario dashboard verificando integracion de componentes
