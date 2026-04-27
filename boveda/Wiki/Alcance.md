# Alcance - Hackathon Centralizador Facturas Packs

## En Alcance

- API REST para carga masiva de facturas (bulk)
- Persistencia en MySQL con control de estados (Pendiente, Proceso, Terminado, Error)
- Endpoints de consulta: lista paginada y resumen de estados
- Integración con AWS SQS (productor y listener)
- Contabilización en tabla `facturas_contabilizadas` del esquema `db_procesos_masivos`
- Endpoint de reintento para facturas en estado Error
- Frontend Angular con:
  - Tabla de facturas con paginación
  - Diagrama de torta con indicador de estados
  - Funcionalidad de carga de archivos CSV

## Fuera de Alcance

- Conexión a TRONADOR (se implementará en un sprint futuro)
- Servicios Comunes de contabilización (APIs de Socio de Negocio, Factura, Recibo de Caja)
- Autenticación y autorización
- Despliegue en ambientes (solo desarrollo local)
- Carga desde bucket S3 (el microservicio expone API REST directamente)

## Roadmap

| Sprint | Entregable |
|--------|-----------|
| **Sprint 1 (actual)** | Microservicio + contabilización en `db_procesos_masivos` + dashboard Angular |
| **Sprint futuro** | Reemplazar escritura en `db_procesos_masivos` por envío a TRONADOR vía Servicios Comunes |
