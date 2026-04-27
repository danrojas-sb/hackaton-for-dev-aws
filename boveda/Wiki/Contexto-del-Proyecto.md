# Contexto del Proyecto - Centralizador Facturas Packs

## Descripción General

Microservicio que forma parte del ecosistema **Centralizador I&E**, encargado de recibir, persistir y gestionar el ciclo de vida de las facturas de packs. El servicio expone un API REST para la carga masiva de facturas, controla sus estados de procesamiento y orquesta el envío asíncrono hacia el esquema `db_procesos_masivos` a través de una cola SQS y una Lambda.

> **Nota**: En este sprint la contabilización se realiza en `db_procesos_masivos` (misma BD del Centralizador). En un sprint futuro se conectará a TRONADOR.

## Problema que Resuelve

Actualmente el flujo de cargue de facturas de packs requiere un punto centralizado que:
- Reciba la información de facturas validada desde archivos CSV
- Persista los registros con trazabilidad de estados
- Orqueste el envío asíncrono hacia la tabla de contabilización
- Provea visibilidad del estado general del proceso mediante un dashboard

## Arquitectura

```mermaid
flowchart TD
    subgraph Frontend["Frontend Angular"]
        A[Tabla de Facturas]
        B[Diagrama de Torta - Estados]
    end

    subgraph Microservicio["Microservicio Spring Boot"]
        C[POST /api/facturas/bulk]
        D[GET /api/facturas]
        E[GET /api/facturas/estados/resumen]
        F[SQS Producer]
        G[SQS Listener - Callback]
    end

    subgraph DB["MySQL - Centralizador"]
        H[(facturas)]
    end

    subgraph AWS["AWS"]
        I[[Cola SQS - Envío]]
        J[[Cola SQS - Respuesta]]
        K[Lambda Orquestador]
    end

    subgraph Contabilizacion["MySQL - db_procesos_masivos"]
        P[(facturas_contabilizadas)]
    end

    Q[(TRONADOR - Sprint futuro)]

    A --> D
    B --> E
    Frontend -->|carga lote| C
    C -->|estado=Pendiente| H
    C --> F --> I --> K
    K -->|contabiliza| P
    K -.->|sprint futuro| Q
    K -->|resultado| J --> G -->|actualiza estado| H
```

## Flujo de Estados

```mermaid
stateDiagram-v2
    [*] --> Pendiente: Factura recibida via API
    Pendiente --> Proceso: Enviada a SQS
    Proceso --> Terminado: Lambda confirma contabilización en TRONADOR
    Proceso --> Error: Fallo en contabilización
    Error --> Pendiente: Reintento
```

| Estado | Descripción |
|--------|-------------|
| **Pendiente** | Factura recibida y persistida, aún no enviada a SQS |
| **Proceso** | Factura enviada a la cola SQS, en espera de respuesta de TRONADOR |
| **Terminado** | Contabilización exitosa en TRONADOR confirmada por la Lambda |
| **Error** | Fallo en el proceso de contabilización, disponible para reintento |

## Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| Backend | Java + Spring Boot |
| Base de datos | MySQL |
| Frontend | Angular |
| Gráficos | ng2-charts (Chart.js) |
| Mensajería | AWS SQS (LocalStack en local) |
| Orquestación | AWS Lambda - Node.js (LocalStack en local) |
| Contabilización | Esquema `db_procesos_masivos` (TRONADOR en sprint futuro) |
| Infraestructura local | Docker Desktop + LocalStack |

## Modelo de Datos

### Tabla `facturas` (esquema centralizador)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT (PK) | Identificador único |
| cus | VARCHAR | Código CUS |
| fecha_pago | DATE | Fecha de pago |
| responsabilidad_fiscal | VARCHAR | Responsabilidad fiscal |
| tipo_documento | VARCHAR | Tipo de documento (CC, CE, etc.) |
| numero_documento | VARCHAR | Número de documento de identidad |
| nombres | VARCHAR | Nombres del cliente |
| apellidos | VARCHAR | Apellidos del cliente |
| telefono | VARCHAR | Teléfono de contacto |
| municipio | VARCHAR | Municipio |
| direccion | VARCHAR | Dirección |
| email | VARCHAR | Correo electrónico |
| valor_pack | DECIMAL | Valor del pack sin IVA |
| valor_pack_iva | DECIMAL | Valor del pack con IVA |
| comentarios | TEXT | Comentarios adicionales |
| estado | ENUM | PENDIENTE, PROCESO, TERMINADO, ERROR |
| fecha_creacion | DATETIME | Fecha de creación del registro |
| fecha_actualizacion | DATETIME | Última actualización |

### Tabla `facturas_contabilizadas` (esquema db_procesos_masivos)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT (PK) | Identificador único |
| cus | VARCHAR | Código CUS |
| fecha_pago | DATE | Fecha de pago |
| responsabilidad_fiscal | VARCHAR | Responsabilidad fiscal |
| tipo_documento | VARCHAR | Tipo de documento |
| numero_documento | VARCHAR | Número de documento de identidad |
| nombres | VARCHAR | Nombres del cliente |
| apellidos | VARCHAR | Apellidos del cliente |
| telefono | VARCHAR | Teléfono de contacto |
| municipio | VARCHAR | Municipio |
| direccion | VARCHAR | Dirección |
| email | VARCHAR | Correo electrónico |
| valor_pack | DECIMAL | Valor del pack sin IVA |
| valor_pack_iva | DECIMAL | Valor del pack con IVA |
| comentarios | TEXT | Comentarios adicionales |
| estado_contable | ENUM | CONTABILIZADA, ERROR_CONTABLE |
| fecha_contabilizacion | DATETIME | Fecha en que se contabilizó |
| factura_origen_id | BIGINT | Referencia a la factura original |

> En un sprint futuro esta tabla será reemplazada por la escritura directa a TRONADOR.

## Endpoints del API

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/facturas/bulk` | Carga masiva de facturas |
| GET | `/api/facturas` | Lista paginada de facturas |
| GET | `/api/facturas/estados/resumen` | Conteo agrupado por estado |
| PUT | `/api/facturas/{id}/reintentar` | Reintento de factura en estado Error |
