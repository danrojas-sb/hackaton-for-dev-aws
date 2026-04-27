# Spec: HU-01 — Carga masiva de facturas

## Requirement

**Como** operador del Centralizador I&E
**Quiero** cargar un lote de facturas a través del API
**Para** que se persistan con estado Pendiente y se inicie su procesamiento

## Acceptance Criteria

### AC-1: El endpoint recibe una lista de facturas en JSON
- **Given** un JSON con una lista de facturas válidas
- **When** se envía POST a `/api/facturas/bulk`
- **Then** responde 200 con la lista de facturas creadas incluyendo id, estado y fechaCreacion

### AC-2: Cada factura se persiste con estado PENDIENTE
- **Given** una lista de N facturas en el request
- **When** se procesan exitosamente
- **Then** se crean N registros en la tabla `centralizador.facturas` con estado = PENDIENTE

### AC-3: Se validan campos obligatorios
- **Given** una factura sin `numeroDocumento`, `nombres` o `valorPack`
- **When** se envía POST a `/api/facturas/bulk`
- **Then** responde 400 con detalle de los campos faltantes

### AC-4: Se validan tipos de datos
- **Given** una factura con `valorPack` negativo o `tipoDocumento` vacío
- **When** se envía POST a `/api/facturas/bulk`
- **Then** responde 400 con detalle de la validación fallida

## API Contract

### POST /api/facturas/bulk

**Request:**
```json
[
  {
    "cus": "8124",
    "fechaPago": "2026-04-21",
    "responsabilidadFiscal": null,
    "tipoDocumento": "CC",
    "numeroDocumento": "1000731504",
    "nombres": "MIGUEL ANGEL",
    "apellidos": "RODRIGUEZ ARANA",
    "telefono": "3116974038",
    "municipio": "BOGOTA",
    "direccion": "BRR BOSA CENTRO CL 63 SUR 79B-43",
    "email": "facturacionpacks@jelpit.com",
    "valorPack": 15750.00,
    "valorPackIva": 18743.00,
    "comentarios": "8124 Cualquier inquietud contactar vía WhatsApp"
  }
]
```

**Response 200:**
```json
[
  {
    "id": 1,
    "cus": "8124",
    "fechaPago": "2026-04-21",
    "tipoDocumento": "CC",
    "numeroDocumento": "1000731504",
    "nombres": "MIGUEL ANGEL",
    "apellidos": "RODRIGUEZ ARANA",
    "telefono": "3116974038",
    "municipio": "BOGOTA",
    "direccion": "BRR BOSA CENTRO CL 63 SUR 79B-43",
    "email": "facturacionpacks@jelpit.com",
    "valorPack": 15750.00,
    "valorPackIva": 18743.00,
    "comentarios": "8124 Cualquier inquietud contactar vía WhatsApp",
    "estado": "PENDIENTE",
    "fechaCreacion": "2026-04-27T10:30:00"
  }
]
```

**Response 400:**
```json
{
  "status": 400,
  "message": "Error de validación",
  "errors": [
    { "field": "numeroDocumento", "message": "no debe estar vacío" }
  ]
}
```

## Technical Design

### Stack
- Java 17 + Spring Boot 3.x + Maven Wrapper
- Spring Data JPA + MySQL 8.0 (puerto 3307 Docker)
- Jakarta Validation (Bean Validation)

### Estructura de archivos a crear

```
proyecto/backend/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/main/java/com/hackathon/centralizador/
│   ├── CentralizadorApplication.java
│   ├── model/
│   │   ├── Factura.java              (entidad JPA)
│   │   └── EstadoFactura.java        (enum)
│   ├── dto/
│   │   ├── FacturaRequest.java       (validaciones)
│   │   └── FacturaResponse.java
│   ├── repository/
│   │   └── FacturaRepository.java
│   ├── service/
│   │   └── FacturaService.java
│   ├── controller/
│   │   └── FacturaController.java
│   └── config/
│       └── CorsConfig.java
├── src/main/resources/
│   └── application.yml
└── src/test/java/com/hackathon/centralizador/
    └── service/
        └── FacturaServiceTest.java
```

### Dependencias (pom.xml)
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- mysql-connector-j
- lombok
- spring-boot-starter-test

### Configuración (application.yml)
- Datasource: `jdbc:mysql://localhost:3307/centralizador`
- JPA: ddl-auto=validate (tablas ya creadas por Docker init.sql)
- Naming strategy: physical (snake_case en BD, camelCase en Java)

## Tasks

- [ ] Task 2: Generar proyecto Spring Boot, entidad Factura, repository, application.yml
- [ ] Task 3: DTO request/response, FacturaService, FacturaController, validaciones, CorsConfig
- [ ] Tests: FacturaServiceTest con Given/When/Then
