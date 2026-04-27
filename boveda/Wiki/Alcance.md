# Alcance - Hackathon Centralizador Facturas Packs

## En Alcance

- API REST para carga masiva de facturas (bulk)
- Persistencia en MySQL con control de estados (Pendiente, Proceso, Terminado, Error)
- Endpoints de consulta: lista paginada y resumen de estados
- Integración con AWS SQS (productor y listener)
- Endpoint de reintento para facturas en estado Error
- Frontend Angular con:
  - Tabla de facturas con paginación
  - Diagrama de torta con indicador de estados
  - Funcionalidad de carga de archivos CSV

## Fuera de Alcance

- Lambda orquestadora (se asume existente)
- Servicios Comunes de contabilización (APIs de Socio de Negocio, Factura, Recibo de Caja)
- Base de datos TRONADOR
- Autenticación y autorización
- Despliegue en ambientes (solo desarrollo local)
- Carga desde bucket S3 (el microservicio expone API REST directamente)
